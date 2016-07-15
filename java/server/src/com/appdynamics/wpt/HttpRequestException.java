/*
 * Copyright (c) AppDynamics Inc
 * All rights reserved
 */
package com.appdynamics.wpt;

public class HttpRequestException extends Exception {
  private int statusCode = -1;

  public HttpRequestException(int statusCode) {
    super("");
    this.statusCode = statusCode;
  }

  public HttpRequestException(String message) {
    super(message);
  }

  public HttpRequestException(String message, Throwable cause) {
    super(message, cause);
  }

  public int getStatusCode() {
    return statusCode;
  }
}
