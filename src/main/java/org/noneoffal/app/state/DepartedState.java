package org.noneoffal.app.state;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.noneoffal.app.entity.Ship;

public class DepartedState implements ShipState {

  private static final Logger LOGGER =
          LogManager.getLogger(DepartedState.class);

  @Override
  public void handle(Ship ship) {
    LOGGER.info("Ship {} departed from the port", ship.getId());
  }

  @Override
  public boolean isFinished() {
    return true;
  }
}