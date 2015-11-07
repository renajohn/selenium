package com.appdynamics.wpt;

/*
 * Copyright (c) AppDynamics Inc
 * All rights reserved
 */
import com.google.common.io.CharStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
public class WptHookClient {
  private final URL hookReadyUrl;
  private final URL webdriverDoneUrl;
  private final URL nextActionUrl;
  private final Logger log;

  public WptHookClient(String wptHookUrl, Logger log) throws MalformedURLException {
    this.log = log;
    this.hookReadyUrl = new URL(wptHookUrl + "/is_hook_ready");
    this.webdriverDoneUrl = new URL(wptHookUrl + "/event/webdriver_done");
    this.nextActionUrl = new URL(wptHookUrl + "/event/next_webdriver_action");
  }

  public void notifyNextWebdriverAction() {
    try {
      log.info("Sending /event/webdriver_done command to the hook");
      getResponse(nextActionUrl);
      log.info("Done sending /event/webdriver_done command to the hook");
    } catch (Exception e) {
      // ignore.
    }
  }

  public boolean isHookReady() {
    String response;
    try {
      log.info("Sending /is_hook_ready query to the hook");
      response = getResponse(hookReadyUrl);
      log.info("Done sending /is_hook_ready query to the hook");
    } catch(IOException e) {
      this.log.info("Can't connect to hook, probably not ready");
      return false;
    }

    JsonObject json = new JsonParser().parse(response).getAsJsonObject();
    log.info(response);
    return (json != null && json.getAsJsonObject("data").getAsJsonPrimitive("ready").getAsBoolean());
  }

  public void notifyWebdriverDone() throws IOException {
    log.info("Sending /event/webdriver_done command to the hook");
    getResponse(webdriverDoneUrl);
    log.info("Done sending /event/webdriver_done command to the hook");
  }

  public String getResponse(URL url) throws IOException {
    log.info("\t Before openConnection");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    log.info("\t After openConnection");
    return CharStreams.toString(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
  }

  public void waitUntilHookReady() {
    while (true) {
      log.info("Waiting for WptHook to be ready...");
      try {
        if (isHookReady()) {
          break;
        }
        Thread.sleep(100);
      } catch (InterruptedException e) {
        throw new RuntimeException("Wait interrupted. Exiting...");
      }
    }
  }
}
