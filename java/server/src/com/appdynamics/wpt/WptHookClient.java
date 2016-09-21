/*
 * Copyright (c) AppDynamics Inc
 * All rights reserved
 */
package com.appdynamics.wpt;

import com.google.common.io.CharStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.openqa.selenium.remote.internal.HttpClientFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

@Deprecated
public class WptHookClient {
  private final Logger logger;
  private final String taskerBaseUrl;
  private final HttpGet browserReadyResource;
  private final HttpGet nextWebDriverActionResource;
  private final HttpGet webDriverDoneResource;
  private final HttpClient client;

  public WptHookClient(String taskerBaseUrl, Logger logger) {
    this.logger = logger;
    this.taskerBaseUrl = taskerBaseUrl;
    this.browserReadyResource = new HttpGet(taskerBaseUrl + "/is_hook_ready");
    this.nextWebDriverActionResource = new HttpGet(taskerBaseUrl + "/event/next_webdriver_action");
    this.webDriverDoneResource = new HttpGet(taskerBaseUrl + "/event/webdriver_done");
    this.client = new HttpClientFactory().getHttpClient();
  }

  @Deprecated
  public boolean isBrowserReady() throws HttpRequestException, IOException {
    logger.info("Attempting GET /is_hook_ready on " + taskerBaseUrl);
    HttpResponse response;

    try {
      response = client.execute(browserReadyResource);
    } catch (IOException e) {
      logger.severe("Failed to connect to WPT hook at: " + taskerBaseUrl);
      throw new HttpRequestException("Error connecting to WPT hook", e);
    }

    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode >= 400) {
      logger.severe("Failed to retrieve browser readiness from WPT hook. statusCode="
                    + statusCode);
      throw new HttpRequestException(statusCode);
    }

    String responseBody = CharStreams.toString(
      new InputStreamReader(response.getEntity().getContent())
    );

    JsonObject jsonObject = new JsonParser().parse(responseBody).getAsJsonObject();

    if (jsonObject != null) {
      logger.info("  -> Response: " + responseBody);
      if (jsonObject.has("data")) {
        JsonObject browserReadiness = jsonObject.getAsJsonObject("data");
        return browserReadiness.getAsJsonPrimitive("ready").getAsBoolean();
      }
    }

    return false;
  }

  @Deprecated
  public void submitWebDriverAction() throws HttpRequestException {
    logger.info("Attempting GET /event/next_webdriver_action on " + taskerBaseUrl);
    HttpResponse response;

    try {
      response = client.execute(nextWebDriverActionResource);
    } catch (IOException e) {
      logger.severe("Failed to connect to WPT hook at: " + taskerBaseUrl);
      throw new HttpRequestException("Error connecting to WPT hook", e);
    }

    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode >= 400) {
      logger.severe("Failed to submit next webdriver action. statusCode=" + statusCode);
      throw new HttpRequestException(statusCode);
    }
  }

  @Deprecated
  public void submitWebdriverDoneAction() throws HttpRequestException {
    logger.info("Attempting GET /event/webdriver_done on " + taskerBaseUrl);
    HttpResponse response;

    try {
      response = client.execute(webDriverDoneResource);
    } catch (IOException e) {
      logger.severe("Failed to connect to WPT hook at: " + taskerBaseUrl);
      throw new HttpRequestException("Error connecting to WPT hook", e);
    }

    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode >= 400) {
      logger.severe("Failed to submit webdriver done event. statusCode=" + statusCode);
      throw new HttpRequestException(statusCode);
    }
  }
}
