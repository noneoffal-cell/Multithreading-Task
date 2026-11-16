package org.noneoffal.app.state;

import org.noneoffal.app.entity.Ship;

public interface ShipState  {

  void handle(Ship ship) throws InterruptedException;

  default boolean isFinished() {
    return false;
  }
}
