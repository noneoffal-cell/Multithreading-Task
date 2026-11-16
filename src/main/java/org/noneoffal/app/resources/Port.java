package org.noneoffal.app.resources;

import org.noneoffal.app.config.PortConfiguration;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class Port {

  private static final int MAX_CONTAINER_COUNT = 100;

  private static final AtomicReference<Port> INSTANCE = new AtomicReference<>();

  private static final ReentrantLock INSTANCE_LOCK = new ReentrantLock();

  private final AtomicInteger currentContainers;

  private Port(PortConfiguration configuration) {
    this.currentContainers = new AtomicInteger(configuration.initialContainers());
  }

  public static Port getInstance(PortConfiguration configuration) {
    Port port = INSTANCE.get();

    if (port == null) {
      INSTANCE_LOCK.lock();
      try {
        port = INSTANCE.get();

        if (port == null) {
          port = new Port(configuration);
          INSTANCE.set(port);
        }
      } finally {
        INSTANCE_LOCK.unlock();
      }
    }
    return port;
  }

  public int getCurrentContainers() {
    return currentContainers.get();
  }
}
