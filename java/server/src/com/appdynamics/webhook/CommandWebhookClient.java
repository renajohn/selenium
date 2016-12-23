/*
 * Copyright (c) AppDynamics Inc
 * All rights reserved
 */
package com.appdynamics.webhook;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.internal.HttpClientFactory;

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
public class CommandWebhookClient {
  private final Logger logger;
  private final HttpClient client;
  private final Gson gson;

  public CommandWebhookClient(Logger logger) {
    this.logger = logger;
    this.client = new HttpClientFactory().getHttpClient();
    this.gson = new Gson();
  }

  public void submitWebDriverCommand(String webhookUrl, Command command) throws HttpRequestException {
    logger.info("Sending next webdriver action: " + command.getName());
    HttpResponse response;

    SAWebDriverAction action = new SAWebDriverAction()
      .withTimestampMs(System.currentTimeMillis())
      .withCommand(command.getName());

    String actionJson = gson.toJson(action);

    HttpPost webhookCommandResource = new HttpPost(webhookUrl);
    webhookCommandResource.setHeader("Content-Type", "application/json");
    webhookCommandResource.setEntity(new StringEntity(actionJson, ContentType.APPLICATION_JSON));

    try {
      response = client.execute(webhookCommandResource);
    } catch (Exception e) {
      logger.severe("Failed to connect to the webhook endpoint at: " + webhookUrl);
      throw new HttpRequestException("Error connecting to webhook endpoint " + webhookUrl, e);
    }

    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode >= 400) {
      logger.severe("Failed to retrieve browser readiness from the webhook endpoint. statusCode="
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
