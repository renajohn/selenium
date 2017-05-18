package org.openqa.selenium.remote.server;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ImmutableCapabilities;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.remote.JsonToBeanConverter;
import org.openqa.selenium.remote.SessionId;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Wraps an existing {@link org.openqa.selenium.WebDriver} instance and provides it with the OSS
 * wire protocol remote end points.
 */
class InMemorySession implements ActiveSession {

  private final SessionId id;
  private final Session session;
  private JsonHttpCommandHandler commandHandler;

  public InMemorySession(SessionId id, Session session, JsonHttpCommandHandler commandHandler) {
    this.id = id;
    this.session = session;
    this.commandHandler = commandHandler;
  }

  @Override
  public String getDescription() {
    return String.format(
        "%s: Legacy Session -> %s",
        id,
        session.getCapabilities().getBrowserName());
  }

  @Override
  public SessionId getId() {
    return id;
  }

  @Override
  public Map<String, Object> getCapabilities() {
    return session.getCapabilities().asMap().entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public void stop() {
    session.close();
  }

  @Override
  public void execute(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    commandHandler.handleRequest(req, resp);
  }

  public static class Factory implements Function<Path, ActiveSession> {

    private final DriverSessions legacySessions;
    private final JsonHttpCommandHandler jsonHttpCommandHandler;

    public Factory(DriverSessions legacySessions) {
      this.legacySessions = Preconditions.checkNotNull(legacySessions);
      jsonHttpCommandHandler = new JsonHttpCommandHandler(
          legacySessions,
          Logger.getLogger(InMemorySession.class.getName()));
    }

    @Override
    public ActiveSession apply(Path path) {
      try (BufferedReader reader = Files.newBufferedReader(path, UTF_8)) {
        Map<?, ?> blob = new JsonToBeanConverter().convert(Map.class, CharStreams.toString(reader));

        Map<String, ?> rawCaps = (Map<String, ?>) blob.get("desiredCapabilities");
        if (rawCaps == null) {
          rawCaps = new HashMap<>();
        }
        Capabilities caps = new ImmutableCapabilities(rawCaps);

        SessionId sessionId = legacySessions.newSession(caps);
        Session session = legacySessions.get(sessionId);

        return new InMemorySession(sessionId, session, jsonHttpCommandHandler);
      } catch (Exception e) {
        throw new SessionNotCreatedException("Unable to create session", e);
      }
    }
  }
}