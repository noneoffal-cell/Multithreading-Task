package org.noneoffal.app.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.noneoffal.app.state.ShipState;

public class Ship implements Runnable {

  private static final Logger LOGGER = LogManager.getLogger(Ship.class);

  private final int id;
  private final int capacity;
  private int currentContainers;
  private final int containersToUnload;
  private final int containersToLoad;

  private ShipState state;

  public Ship(
          int id,
          int capacity,
          int currentContainers,
          int containersToUnload,
          int containersToLoad)
  {
    this.id = id;
    this.capacity = capacity;
    this.currentContainers = currentContainers;
    this.containersToUnload = containersToUnload;
    this.containersToLoad = containersToLoad;
  }

  public int getId() {
    return id;
  }

  public int getCapacity() {
    return capacity;
  }

  public int getCurrentContainers() {
    return currentContainers;
  }

  public int getContainersToUnload() {
    return containersToUnload;
  }

  public int getContainersToLoad() {
    return containersToLoad;
  }

  public void unloadContainers(int count) {
    if (count < 0 || count > currentContainers) {
      throw new IllegalArgumentException("Invalid number of containers to unload");
    }
    currentContainers -= count;
    LOGGER.info("Ship {} unloaded {} containers. Current containers: {}", id, count, currentContainers);
  }

  public void loadContainers(int count) {
    if (count < 0 || currentContainers + count > capacity) {
      throw new IllegalArgumentException("Invalid number of containers to load");
    }
    currentContainers += count;
    LOGGER.info("Ship {} loaded {} containers. Current containers: {}", id, count, currentContainers);
  }

  public void setState(ShipState state) {
    this.state = state;
  }

  @Override
  public void run() {
    if (state == null) {
      throw new IllegalStateException("Initial state is not set");
    }

    try {
      while (true) {
        ShipState currentState = state;
        currentState.handle(this);

        if (currentState.isFinished()) {
          break;
        }
      }
    } catch (InterruptedException _) {
      Thread.currentThread().interrupt();
    }
  }
}
