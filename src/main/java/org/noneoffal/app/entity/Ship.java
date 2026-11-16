package org.noneoffal.app.entity;

import java.util.concurrent.Callable;

public class Ship implements Callable<ShipResult> {

  private final int id;
  private final int capacity;
  private int currentContainers;
  private final int containersToUnload;
  private final int containersToLoad;

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

  @Override
  public ShipResult call() throws Exception {
    return null;
  }
}
