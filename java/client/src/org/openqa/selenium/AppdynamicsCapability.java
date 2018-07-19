package org.openqa.selenium;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

// This class encompasses AppDynamics specific capabilities that modify the behavior of
// the standalone selenium webdriver server when run in the context of the Synthetic Agent.
//
// Copyright 2016 AppDynamics inc. All rights reserved
public class AppdynamicsCapability {
  public static final String APPDYNAMICS_CAPABILITIES = "appdynamicsCapability";

  // Capability key that specifies the URL to forward the webdriver commands.
  private static final String COMMAND_WEBHOOK = "commandWebhook";
  // Capability key that specifies the test output directory.
  private static final String OUTPUT_DIR = "outputDir";
  // Capability key that specifies the test ID.
  private static final String TEST_ID = "testId";
  // Capability key that specifies whether the current session should flush all existing
  // sessions before creating a new one.
  private static final String FLUSH_SESSIONS = "flushSessions";

  // Default endpoint for forwarding webdriver commands.
  private static final String DEFAULT_COMMAND_WEBHOOK = "http://localhost:8888/v1/webdriver/action";

  private String outputDir;
  private String testID;
  private String commandWebhook = DEFAULT_COMMAND_WEBHOOK;
  private boolean flushSessions;

  public AppdynamicsCapability(Map<String, String> map) {
    if (map.containsKey(OUTPUT_DIR) && !Strings.isNullOrEmpty(map.get(OUTPUT_DIR))) {
      this.outputDir = map.get(OUTPUT_DIR);
    }
    if (map.containsKey(TEST_ID) && !Strings.isNullOrEmpty(map.get(TEST_ID))) {
      this.testID = map.get(TEST_ID);
    }
    if (map.containsKey(COMMAND_WEBHOOK) && !Strings.isNullOrEmpty(map.get(COMMAND_WEBHOOK))) {
      this.commandWebhook = map.get(COMMAND_WEBHOOK);
    }
    if (map.containsKey(FLUSH_SESSIONS) && !Strings.isNullOrEmpty(map.get(FLUSH_SESSIONS))) {
      this.flushSessions = Boolean.parseBoolean(map.get(FLUSH_SESSIONS));
    }
  }

  public AppdynamicsCapability() {
    this.commandWebhook = "http://localhost:8888/v1/webdriver/action";
  }

  public String getTestID() {
    return testID;
  }

  public String getOutputDir() {
    return outputDir;
  }

  public String getCommandWebhook() {
    return this.commandWebhook;
  }

  public boolean flushSessions() {
    return this.flushSessions;
  }

  public ImmutableMap<String, String> getEnvironment() {
    ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
    if (outputDir != null) {
      mapBuilder.put("K9_OUTPUT_DIR", outputDir);
    }
    if (testID != null) {
      mapBuilder.put("K9_TEST_ID", testID);
    }
    return mapBuilder.build();
  }

  public static AppdynamicsCapability extractFrom(Capabilities capabilities) {
    return extractFrom((Map<String, Object>) capabilities.asMap());
  }

  public static AppdynamicsCapability extractFrom(Map<String, Object> capabilitiesMap) {
    Object rawAppdynamicsCapability = capabilitiesMap.get(APPDYNAMICS_CAPABILITIES);
    AppdynamicsCapability appdynamicsCapability = new AppdynamicsCapability();
    if (rawAppdynamicsCapability != null) {
      if (rawAppdynamicsCapability instanceof AppdynamicsCapability) {
        appdynamicsCapability = (AppdynamicsCapability) rawAppdynamicsCapability;
      } else if (rawAppdynamicsCapability instanceof Map) {
        appdynamicsCapability = new AppdynamicsCapability((Map<String, String>) rawAppdynamicsCapability);
      } else {
        throw new RuntimeException(
          String.format("Failed to parse AppDynamics capabilities correctly."
                        + " List of capabilities is %s", capabilitiesMap.toString()));
      }
    }
    return appdynamicsCapability;
  }
}
