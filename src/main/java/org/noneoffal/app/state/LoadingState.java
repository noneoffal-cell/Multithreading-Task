package org.noneoffal.app.state;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.noneoffal.app.entity.Berth;
import org.noneoffal.app.entity.Ship;
import org.noneoffal.app.resources.Port;

public class LoadingState implements ShipState {

  private static final Logger LOGGER =
          LogManager.getLogger(LoadingState.class);

  private final Port port;
  private final ShipState nextState;

  public LoadingState(Port port, ShipState nextState) {
    this.port = port;
    this.nextState = nextState;
  }

  @Override
  public void handle(Ship ship) throws InterruptedException {
    int containersToLoad = ship.getContainersToLoad();

    if (containersToLoad == 0) {
      ship.setState(nextState);
      return;
    }
    port.reserveContainers(containersToLoad);
    Berth berth = null;
    boolean loadingCompleted = false;

    try {
      berth = port.getBerth();
      ship.loadContainers(containersToLoad);
      port.completeLoading(containersToLoad);
      loadingCompleted = true;
      LOGGER.info("Ship {} completed loading at berth {}", ship.getId(), berth.id());
    } finally {
      try {
        if (berth != null) {
          port.releaseBerth(berth);
        }
      } finally {
        if (!loadingCompleted) {
          port.cancelContainerReservation(containersToLoad);
        }
      }
    }
    ship.setState(nextState);
  }
}
