/*
 * Copyright (c) AppDynamics Inc
 * All rights reserved
 */
package com.appdynamics.wpt;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.internal.HttpClientFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.logging.Logger;

/**
 * @author <a mailto="renault.lecoultre@appdynamics.com">Renault John Lecoultre</a>
 * @since 6/26/2015
 *
 * @author <a mailto="karthik3186@gmail.com">Karthik Krishnamurthy</a>
 * @since 2/26/2015
 *
 * Copyright 2015 AppDynamics inc. All rights reserved
 */
public class SATaskerClient {
  private final Logger logger;
  private final HttpClient client;
  private final HttpGet browserReadyResource;
  private final HttpPost webdriverActionResource;
  private final String taskerBaseUrl;
  private final Gson gson;

  public SATaskerClient(String taskerBaseUrl, Logger logger) {
    this.logger = logger;
    this.taskerBaseUrl = taskerBaseUrl;
    this.browserReadyResource = new HttpGet(taskerBaseUrl + "/v1/browser/ready");
    this.webdriverActionResource = new HttpPost(taskerBaseUrl + "/v1/webdriver/action");
    this.client = new HttpClientFactory().getHttpClient();
    this.gson = new Gson();
  }

  public boolean isBrowserReady() throws IOException, HttpRequestException {
    logger.info("Attempting GET /v1/browser/ready on " + taskerBaseUrl);
    HttpResponse response;

    try {
      response = client.execute(browserReadyResource);
    } catch (Exception e) {
      logger.severe("Failed to connect to the synthetic tasker at: " + taskerBaseUrl);
      throw new HttpRequestException("Error connecting to synthetic tasker", e);
    }

    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode >= 400) {
      logger.severe("Failed to retrieve browser readiness from the tasker. statusCode="
                    + statusCode);
      throw new HttpRequestException(statusCode);
    }

    String responseBody = CharStreams.toString(
      new InputStreamReader(response.getEntity().getContent())
    );

    JsonObject browserReadiness = new JsonParser().parse(responseBody).getAsJsonObject();

    if (browserReadiness != null) {
      logger.info("  -> Response: " + responseBody);
      if (browserReadiness.has("ready")) {
        return browserReadiness.getAsJsonPrimitive("ready").getAsBoolean();
      }
    }

    return false;
  }

  public void submitWebDriverAction(Command command) throws HttpRequestException {
    logger.info("Sending next webdriver action: " + command.getName());
    HttpResponse response;

    SAWebDriverAction action = new SAWebDriverAction()
      .withTimestampMs(System.currentTimeMillis())
      .withCommand(command.getName());

    String actionJson = gson.toJson(action);

    webdriverActionResource.setHeader("Content-Type", "application/json");
    webdriverActionResource.setEntity(new StringEntity(actionJson, ContentType.APPLICATION_JSON));

    try {
      response = client.execute(webdriverActionResource);
    } catch (Exception e) {
      logger.severe("Failed to connect to the synthetic tasker at: " + taskerBaseUrl);
      throw new HttpRequestException("Error connecting to synthetic tasker", e);
    }

    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode >= 400) {
      logger.severe("Failed to retrieve browser readiness from the tasker. statusCode="
                    + statusCode);
      throw new HttpRequestException(statusCode);
    }

    logger.info("WebDriver action sent successfully. statusCode=" + statusCode);
  }

  // TODO: Should try and re-use the one in synthetic-tasker-api after adding it as a dependency.
  private static class SAWebDriverAction {
    public Long timestampMs;
    public String command;

    @Override
    public String toString() {
      return "SAWebDriverAction{" +
             "timestampMs=" + timestampMs +
             ", command='" + command + '\'' +
             '}';
    }

    public SAWebDriverAction withTimestampMs(Long timestampMs) {
      this.timestampMs = timestampMs;
      return this;
    }

    public SAWebDriverAction withCommand(String command) {
      this.command = command;
      return this;
    }
  }
}
