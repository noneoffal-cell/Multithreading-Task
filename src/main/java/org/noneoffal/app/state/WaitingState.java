package org.noneoffal.app.state;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.noneoffal.app.entity.Ship;
import org.noneoffal.app.resources.Port;

public class WaitingState implements ShipState {

  private static final Logger LOGGER =
          LogManager.getLogger(WaitingState.class);

  private final Port port;

  public WaitingState(Port port) {
    this.port = port;
  }

  @Override
  public void handle(Ship ship) {
    ShipState nextState = new DepartedState();
    if (ship.getContainersToLoad() > 0) {
      nextState = new LoadingState(port, nextState);
    }
    if (ship.getContainersToUnload() > 0) {
      nextState = new UnloadingState(port, nextState);
    }
    ship.setState(nextState);
    LOGGER.info("Ship {} is ready for service", ship.getId());
  }
}
