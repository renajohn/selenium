package org.openqa.selenium;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Created by shashwat.srivastava on 5/30/17.
 */
public class AppdynamicsCapabilities {
  /**
   * AppDynamics extensions
   *
   * This capability forwards all commands to a specified end point before executing them.
   *
   * This capability affects the remote webdriver server, not the clients.
   *
   * @author Renault John Lecoultre <renault.lecoultre@appdynamics.com>
   * @since 21/10/16
   * Copyright 2016 AppDynamics inc. All rights reserved
   */
  public static  String COMMAND_WEBHOOK = "commandWebhook";
  /**
   * This capability sets the K9 output directory.
   */
  public static String K9_OUTPUT_DIR = "outputDir";
  /**
   * This capability sets the K9 test id
   */
  public static String K9_TEST_ID = "testId";

  private String outputDir;
  private String testID;
  private String commandWebhook;
  /**
   * AppDynamics extension
   *
   * This capability will make it possible to set environment variables for
   * the K9 process
   */
  public static final String APPDYNAMICS_CAPABILITIES = "appdynamicsCapabilities";

  public AppdynamicsCapabilities(Map<String, String> map) {
    if (map.containsKey(K9_OUTPUT_DIR) && map.get(K9_OUTPUT_DIR) != null) {
      this.outputDir = map.get(K9_OUTPUT_DIR);
    }
    if (map.containsKey(K9_TEST_ID) && map.get(K9_TEST_ID) != null) {
      this.testID = map.get(K9_TEST_ID);
    }
    if (map.containsKey(COMMAND_WEBHOOK) && map.get(COMMAND_WEBHOOK) != null) {
      this.commandWebhook = map.get(COMMAND_WEBHOOK);
    }
  }

  public AppdynamicsCapabilities() {
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

  public ImmutableMap<String, String> getK9Environment() {
    ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
    if (outputDir != null) {
      mapBuilder.put("K9_OUTPUT_DIR", outputDir);
    }
    if (testID != null) {
      mapBuilder.put("K9_TEST_ID", testID);
    }
    return mapBuilder.build();
  }

  public static AppdynamicsCapabilities extractFrom(Capabilities capabilities) {
    Object rawK9Variables = capabilities.getCapability(APPDYNAMICS_CAPABILITIES);
    AppdynamicsCapabilities appdynamicsCapabilities = new AppdynamicsCapabilities();
    if (rawK9Variables != null) {
      if (rawK9Variables instanceof AppdynamicsCapabilities) {
        appdynamicsCapabilities = (AppdynamicsCapabilities) rawK9Variables;
      } else if (rawK9Variables instanceof Map) {
        appdynamicsCapabilities = new AppdynamicsCapabilities((Map<String, String>) rawK9Variables);
      } else {
        throw new RuntimeException(
          String.format("Failed to parse appdynamics capabilities correctly. List of capabilities is %s", capabilities.toString()));
      }
    }
    return appdynamicsCapabilities;
  }
}
