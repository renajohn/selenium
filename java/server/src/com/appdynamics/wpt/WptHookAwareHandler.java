/*
 * Copyright (c) AppDynamics Inc
 * All rights reserved
 */
package com.appdynamics.wpt;

import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.Command;

import java.io.IOException;
import java.util.logging.Logger;
import java.net.MalformedURLException;

/**
 * @author <a mailto="renault.lecoultre@appdynamics.com">Renault John Lecoultre</a>
 * @since 6/26/2015
 *
 * @author <a mailto="karthik3186@gmail.com">Karthik Krishnamurthy</a>
 * @since 2/14/2015
 */
public class WptHookAwareHandler {
  private final Logger log;
  private WptHookClient wptHookClient;

  public WptHookAwareHandler(Logger log, String url) {
    try {
      wptHookClient = new WptHookClient(url, log);
    } catch (MalformedURLException e) {
      log.severe("Failed to initialize WptHookClient: " + e.getMessage());
    }
    this.log = log;
  }

  public boolean waitIfNeeded(Command command) throws Exception {
    boolean hasWaited = false;
    if (command.getName().equals(DriverCommand.QUIT) || command.getName().equals(DriverCommand.CLOSE)) {
      // we need to inform WPT that the session will terminate
      wptHookClient.webdriverDone();
    }

    if (!command.getName().equals(DriverCommand.NEW_SESSION)) {
      // Check if the Wpt Hook is ready before executing the command.
      waitUntilHookReady();
      hasWaited = true;
    }
    // else, this is the NEW_SESSION command which spawns the browser in the first place. So, checking with the
    // hook doesn't make sense. Allow the command to go through.
    log.info("Executing driver command: {} " + command.toString());
    return hasWaited;
  }

  public WptHookClient getWptHookClient() {
    return wptHookClient;
  }

  private void waitUntilHookReady() {
    while (true) {
      log.info("Waiting for WptHook to be ready...");
      try {
        if (wptHookClient.isHookReady()) {
          break;
        }
        Thread.sleep(100);
      } catch (IOException e) {
        log.severe("Unable to get response for 'is_hook_ready' request!");
        throw new RuntimeException("Cannot reach WptHook", e);
      } catch (InterruptedException e) {
        throw new RuntimeException("Wait interrupted. Exiting...");
      }
    }
  }
}
