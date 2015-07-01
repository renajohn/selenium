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
  private final Logger log;

  public WptHookClient(String wptHookUrl, Logger log) throws MalformedURLException {
    this.log = log;
    this.hookReadyUrl = new URL(wptHookUrl + "/is_hook_ready");
    this.webdriverDoneUrl = new URL(wptHookUrl + "/event/webdriver_done");
  }

  public boolean isHookReady() throws IOException {
    String response = getResponse(hookReadyUrl);
    JsonObject json = new JsonParser().parse(response).getAsJsonObject();
    log.info(response);
    return (json != null && json.getAsJsonObject("data").getAsJsonPrimitive("ready").getAsBoolean());
  }

  public void webdriverDone() throws IOException {
    getResponse(webdriverDoneUrl);
  }

  public static String getResponse(URL url) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    return CharStreams.toString(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
  }
}
