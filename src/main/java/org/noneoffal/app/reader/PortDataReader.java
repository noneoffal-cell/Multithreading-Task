package org.noneoffal.app.reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PortDataReader {

  private static final Logger LOGGER =
          LogManager.getLogger(PortDataReader.class);

  public List<String> read(String resourceName) throws IOException {
    if (resourceName == null || resourceName.isBlank()) {
      throw new IllegalArgumentException("Resource name must not be blank");
    }

    InputStream inputStream = getClass()
            .getClassLoader()
            .getResourceAsStream(resourceName);

    if (inputStream == null) {
      throw new IOException("Resource not found: " + resourceName);
    }

    try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

      List<String> lines = new ArrayList<>();
      String line;

      while ((line = reader.readLine()) != null) {
        lines.add(line);
      }

      LOGGER.info("Read {} lines from {}", lines.size(), resourceName);

      return List.copyOf(lines);

    } catch (IOException e) {
      LOGGER.error("Failed to read resource: {}", resourceName, e);
      throw e;
    }
  }
}