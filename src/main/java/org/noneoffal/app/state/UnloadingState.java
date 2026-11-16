package org.noneoffal.app.state;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.noneoffal.app.entity.Berth;
import org.noneoffal.app.entity.Ship;
import org.noneoffal.app.resources.Port;

public class UnloadingState implements ShipState {

  private static final Logger LOGGER =
          LogManager.getLogger(UnloadingState.class);

  private final Port port;
  private final ShipState nextState;

  public UnloadingState(Port port, ShipState nextState) {
    this.port = port;
    this.nextState = nextState;
  }

  @Override
  public void handle(Ship ship) throws InterruptedException {
    int containersToUnload = ship.getContainersToUnload();
    if (containersToUnload == 0) {
      ship.setState(nextState);
      return;
    }
    port.reserveSpace(containersToUnload);
    Berth berth = null;
    boolean unloadingCompleted = false;

    try {
      berth = port.getBerth();
      ship.unloadContainers(containersToUnload);
      port.completeUnloading(containersToUnload);
      unloadingCompleted = true;
      LOGGER.info("Ship {} completed unloading at berth {}", ship.getId(), berth.id());
    } finally {
      try {
        if (berth != null) {
          port.releaseBerth(berth);
        }
      } finally {
        if (!unloadingCompleted) {
          port.cancelSpaceReservation(containersToUnload);
        }
      }
    }
    ship.setState(nextState);
  }
}
