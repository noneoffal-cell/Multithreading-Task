package org.noneoffal.app.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.noneoffal.app.config.PortConfiguration;
import org.noneoffal.app.config.ShipConfiguration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PortDataParser {

  private static final Logger LOGGER =
          LogManager.getLogger(PortDataParser.class);

  public PortConfiguration parsePortConfiguration(List<String> lines) {
    PortConfiguration configuration = null;

    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);

      if (!line.isBlank()) {
        String[] fields = splitLine(line);

        if (isRecord(fields, "PORT")) {
          if (configuration != null) {
            throw new IllegalArgumentException(
                    "Duplicate port configuration"
            );
          }

          configuration = parsePortFields(fields, i + 1);
        }
      }
    }

    if (configuration == null) {
      throw new IllegalArgumentException("Port configuration not found");
    }

    LOGGER.info("Port configuration parsed successfully");

    return configuration;
  }

  public List<ShipConfiguration> parseShipConfigurations(List<String> lines) {
    List<ShipConfiguration> configurations = new ArrayList<>();
    Set<Integer> shipIds = new HashSet<>();

    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);

      if (!line.isBlank()) {
        String[] fields = splitLine(line);

        if (isRecord(fields, "SHIP")) {
          ShipConfiguration configuration =
                  parseShipFields(fields, i + 1);

          if (!shipIds.add(configuration.id())) {
            throw new IllegalArgumentException(
                    "Duplicate ship ID: " + configuration.id()
            );
          }

          configurations.add(configuration);
        }
      }
    }

    if (configurations.isEmpty()) {
      throw new IllegalArgumentException("No ship configurations found");
    }

    LOGGER.info(
            "{} ship configurations parsed successfully",
            configurations.size()
    );

    return List.copyOf(configurations);
  }

  private PortConfiguration parsePortFields(String[] fields, int lineNumber) {
    if (fields.length != 3) {
      throw new IllegalArgumentException(
              "Invalid port configuration at line " + lineNumber
      );
    }

    int initialContainers = parseInteger(fields[1], lineNumber);
    int berthCount = parseInteger(fields[2], lineNumber);

    if (initialContainers < 0 || berthCount <= 0) {
      throw new IllegalArgumentException(
              "Invalid port configuration values at line " + lineNumber
      );
    }

    return new PortConfiguration(initialContainers, berthCount);
  }

  private ShipConfiguration parseShipFields(
          String[] fields,
          int lineNumber
  ) {
    if (fields.length != 6) {
      throw new IllegalArgumentException(
              "Invalid ship configuration at line " + lineNumber
      );
    }

    int id = parseInteger(fields[1], lineNumber);
    int capacity = parseInteger(fields[2], lineNumber);
    int initialContainers = parseInteger(fields[3], lineNumber);
    int containersToUnload = parseInteger(fields[4], lineNumber);
    int containersToLoad = parseInteger(fields[5], lineNumber);

    validateShipValues(
            id,
            capacity,
            initialContainers,
            containersToUnload,
            containersToLoad,
            lineNumber
    );

    return new ShipConfiguration(
            id,
            capacity,
            initialContainers,
            containersToUnload,
            containersToLoad
    );
  }

  private void validateShipValues(
          int id,
          int capacity,
          int initialContainers,
          int containersToUnload,
          int containersToLoad,
          int lineNumber
  ) {
    if (id <= 0) {
      throw new IllegalArgumentException("Invalid ship ID at line " + lineNumber);
    }

    if (capacity <= 0) {
      throw new IllegalArgumentException("Invalid ship capacity at line " + lineNumber);
    }

    if (initialContainers < 0 || initialContainers > capacity) {
      throw new IllegalArgumentException(
              "Invalid initial container count at line " + lineNumber
      );
    }

    if (containersToUnload < 0 || containersToUnload > initialContainers) {
      throw new IllegalArgumentException(
              "Invalid unloading count at line " + lineNumber
      );
    }

    if (containersToLoad < 0) {
      throw new IllegalArgumentException(
              "Invalid loading count at line " + lineNumber
      );
    }

    long finalContainers =
            (long) initialContainers - containersToUnload + containersToLoad;

    if (finalContainers > capacity) {
      throw new IllegalArgumentException(
              "Ship capacity exceeded at line " + lineNumber
      );
    }
  }

  private boolean isRecord(String[] fields, String recordType) {
    return fields[0].equals(recordType);
  }

  private String[] splitLine(String line) {
    String[] fields = line.split(",", -1);

    for (int i = 0; i < fields.length; i++) {
      fields[i] = fields[i].trim();
    }

    return fields;
  }

  private int parseInteger(String value, int lineNumber) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
              "Invalid number at line " + lineNumber,
              e
      );
    }
  }
}