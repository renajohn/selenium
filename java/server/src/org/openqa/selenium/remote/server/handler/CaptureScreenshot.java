// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.remote.server.handler;

import static org.openqa.selenium.OutputType.BYTES;

import com.google.common.io.Files;

import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.internal.Base64Encoder;
import org.openqa.selenium.remote.server.Session;

import java.io.File;
import java.io.IOException;

public class CaptureScreenshot extends WebDriverHandler<String> {

  private File dumpDir;

  public CaptureScreenshot(Session session) {
    super(session);

    String path = System.getProperty("webdriver.saveScreenshots");
    if (path != null && !path.isEmpty()) {
      dumpDir = new File(path);
      dumpDir.mkdirs();
    }
  }

  @Override
  public String call() throws Exception {
    WebDriver driver = getUnwrappedDriver();

    byte[] png = ((TakesScreenshot) driver).getScreenshotAs(BYTES);

    if (dumpDir != null) {
      save(dumpDir, png);
    }
    return new Base64Encoder().encode(png);
  }

  @Override
  public String toString() {
    return "[take screenshot]";
  }

  private File save(File folder, byte[] data) {
    try {
      String filename = System.currentTimeMillis() + "_screenshot.png";

      File file = new File(folder, filename);
      Files.write(data, file);

      return file;
    } catch (IOException e) {
      throw new WebDriverException(e);
    }
  }
}
