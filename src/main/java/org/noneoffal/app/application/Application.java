package org.noneoffal.app.application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.noneoffal.app.config.PortConfiguration;
import org.noneoffal.app.config.ShipConfiguration;
import org.noneoffal.app.entity.Ship;
import org.noneoffal.app.parser.PortDataParser;
import org.noneoffal.app.reader.PortDataReader;
import org.noneoffal.app.resources.Port;
import org.noneoffal.app.state.WaitingState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Application {

  private static final Logger LOGGER =
          LogManager.getLogger(Application.class);

  private static final String DATA_FILE = "port-data.txt";

  private final PortDataReader reader = new PortDataReader();
  private final PortDataParser parser = new PortDataParser();

  public void run() throws IOException, InterruptedException {
    List<String> lines = reader.read(DATA_FILE);

    PortConfiguration portConfiguration =
            parser.parsePortConfiguration(lines);

    List<ShipConfiguration> shipConfigurations =
            parser.parseShipConfigurations(lines);

    Port port = Port.getInstance(portConfiguration);

    List<Thread> shipThreads =
            createShipThreads(shipConfigurations, port);

    startShipThreads(shipThreads);
    waitForShipThreads(shipThreads);

    LOGGER.info("All ships have finished their work");
  }

  private List<Thread> createShipThreads(
          List<ShipConfiguration> configurations,
          Port port
  ) {
    List<Thread> threads = new ArrayList<>();

    for (ShipConfiguration configuration : configurations) {
      Ship ship = new Ship(
              configuration.id(),
              configuration.capacity(),
              configuration.initialContainers(),
              configuration.containersToUnload(),
              configuration.containersToLoad()
      );

      ship.setState(new WaitingState(port));

      Thread thread = new Thread(ship, "Ship-" + ship.getId());

      thread.setUncaughtExceptionHandler((failedThread, exception) ->
              LOGGER.error(
                      "Uncaught exception in {}",
                      failedThread.getName(),
                      exception
              )
      );

      threads.add(thread);
    }

    return threads;
  }

  private void startShipThreads(List<Thread> threads) {
    for (Thread thread : threads) {
      LOGGER.info("Starting {}", thread.getName());
      thread.start();
    }
  }

  private void waitForShipThreads(List<Thread> threads)
          throws InterruptedException {
    try {
      for (Thread thread : threads) {
        thread.join();
      }
    } catch (InterruptedException e) {
      for (Thread thread : threads) {
        thread.interrupt();
      }

      throw e;
    }
  }
}