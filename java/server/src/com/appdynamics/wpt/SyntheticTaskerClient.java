/*
 * Copyright (c) AppDynamics Inc
 * All rights reserved
 */
package com.appdynamics.wpt;

import org.openqa.selenium.remote.Command;

import java.io.IOException;
import java.util.logging.Logger;

public class SyntheticTaskerClient {
  private final Logger logger;
  private final String taskerBaseUrl;
  private final WptHookClient wptHookClient;
  private final SATaskerClient taskerClient;
  private int taskerVersion = -1;

  public SyntheticTaskerClient(String taskerBaseUrl, Logger logger) {
    this.taskerBaseUrl = taskerBaseUrl;
    this.logger = logger;
    this.taskerClient = new SATaskerClient(taskerBaseUrl, logger);
    this.wptHookClient = new WptHookClient(taskerBaseUrl, logger);
  }

  public void waitForBrowserReady(long timeoutMs) {
    long waited = 0L;

    while (timeoutMs == -1L || waited < timeoutMs) {
      logger.info("Waiting for browser to be ready...");
      try {
        if (isBrowserReady()) {
          logger.info("Browser is NOW ready!");
          return;
        }
        Thread.sleep(100);
        waited += 100L;
      } catch (InterruptedException e) {
        logger.severe("  -> Interrupted while waiting for the browser to be ready");
        throw new RuntimeException("Interrupted while waiting for the browser to be ready");
      } catch (HttpRequestException|IOException e) {
        throw new RuntimeException("Unable to query the browser for readiness", e);
      }
    }
  }

  public void submitWebDriverNextAction(Command command) throws HttpRequestException {
    if (taskerVersion == -1) {
      determineTaskerVersion();
    }

    if (taskerVersion == 1) {
      wptHookClient.submitWebDriverAction();
    } else {
      taskerClient.submitWebDriverAction(command);
    }
  }

  public void submitWebDriverDoneAction(Command command) throws HttpRequestException {
    if (taskerVersion == -1) {
      determineTaskerVersion();
    }

    if (taskerVersion == 1) {
      wptHookClient.submitWebdriverDoneAction();
    } else {
      taskerClient.submitWebDriverAction(command);
    }
  }

  private boolean isBrowserReady() throws IOException, HttpRequestException {
    if (taskerVersion == -1) {
      determineTaskerVersion();
    }

    if (taskerVersion == 1) {
      return wptHookClient.isBrowserReady();
    } else {
      return taskerClient.isBrowserReady();
    }
  }

  private void determineTaskerVersion() {
    logger.info("Attempting to determine the synthetic tasker version. Trying with v2 first...");
    // First try with V2.
    try {
      boolean isBrowserReady = taskerClient.isBrowserReady();
      // V2 URL works. Assume V2 going forward.
      logger.info("Found v2 endpoint working!");
      taskerVersion = 2;
      return;
    } catch (IOException e) {
      // Parsing error in the response. Assume V2 as the URL still works.
      logger.info("Found v2 endpoint working!");
      taskerVersion = 2;
      return;
    } catch (HttpRequestException e) {
      if (e.getStatusCode() != 403 && e.getStatusCode() != 404) {
        // Old hook returns 403 instead of 404. So, check for that as well.
        throw new RuntimeException("Error while communicating with the tasker", e);
      }
    }

    logger.info("Trying with v1 now...");
    // Now try with V1.
    try {
      boolean isBrowserReady = wptHookClient.isBrowserReady();
      // Assume V1 going forward.
      logger.info("Found v1 endpoint working!");
      taskerVersion = 1;
      return;
    } catch (IOException e) {
      // Parsing error in the response. Assume V2 as the URL still works.
      logger.info("Found v1 endpoint working!");
      taskerVersion = 1;
      return;
    } catch (HttpRequestException e) {
      throw new RuntimeException("Error while communicating with the tasker", e);
    }
  }
}
