package org.noneoffal.app.resources;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.noneoffal.app.config.PortConfiguration;
import org.noneoffal.app.entity.Berth;

import java.util.ArrayDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class Port {

  private static final Logger LOGGER = LogManager.getLogger(Port.class);

  private static final int MAX_CONTAINER_COUNT = 100;

  private static final AtomicReference<Port> INSTANCE = new AtomicReference<>();

  private static final ReentrantLock instanceLock = new ReentrantLock();

  private final AtomicInteger storedContainerCount;
  private final Semaphore containersAvailableForLoading;
  private final Semaphore freeContainerSlots;

  private final ArrayDeque<Berth> availableBerths;

  private final ReentrantLock berthLock = new ReentrantLock(true);
  private final Condition berthAvailable = berthLock.newCondition();

  private Port(PortConfiguration configuration) {
    int initialContainers = configuration.initialContainers();
    int berthCount = configuration.berthCount();

    if (initialContainers < 0 || initialContainers > MAX_CONTAINER_COUNT) {
      throw new IllegalArgumentException("Invalid initial container count");
    }

    if (berthCount <= 0) {
      throw new IllegalArgumentException("Berth count must be positive");
    }

    this.storedContainerCount = new AtomicInteger(initialContainers);
    this.containersAvailableForLoading = new Semaphore(initialContainers, true);
    this.freeContainerSlots = new Semaphore(MAX_CONTAINER_COUNT - initialContainers, true);
    this.availableBerths = new ArrayDeque<>();

    for (int i = 1; i <= berthCount; i++) {
      availableBerths.addLast(new Berth(i));
    }

    LOGGER.info("Port initialized. Containers: {}. Berths. {}", initialContainers, berthCount);
  }

  // Doing double-checked locking
  public static Port getInstance(PortConfiguration configuration) {
    Port port = INSTANCE.get(); // the first check

    if (port == null) {
      instanceLock.lock();
      try {
        port = INSTANCE.get(); // the second check

        if (port == null) {
          port = new Port(configuration);
          INSTANCE.set(port);
        }
      } finally {
        instanceLock.unlock();
      }
    }
    return port;
  }

  public Berth getBerth() throws InterruptedException {
    berthLock.lockInterruptibly();

    try {
      while (availableBerths.isEmpty()) {
        LOGGER.debug("No free berths. Waiting...");
        berthAvailable.await();
      }
      return availableBerths.poll();
    } finally {
      berthLock.unlock();
    }
  }

  public void releaseBerth(Berth berth) {
    berthLock.lock();

    try {
      if (availableBerths.contains(berth)) {
        throw new IllegalStateException("Berth is already available");
      }
      availableBerths.offer(berth);
      berthAvailable.signal();
    } finally {
      berthLock.unlock();
    }
  }

  public void reserveSpace(int count) throws InterruptedException {
    validateCount(count);
    freeContainerSlots.acquire(count);
    LOGGER.debug("Reserved {} slots for unloading", count);
  }

  public void cancelSpaceReservation(int count) {
    validateCount(count);
    freeContainerSlots.release(count);
    LOGGER.debug("Canceled reservation of {} unloading slots", count);
  }

  public void completeUnloading(int count) {
    validateCount(count);
    int total = storedContainerCount.addAndGet(count);
    containersAvailableForLoading.release(count);
    LOGGER.info("Unloaded {} containers. Containers in port: {}/{}", count, total, MAX_CONTAINER_COUNT);
  }

  public void reserveContainers(int count) throws InterruptedException {
    validateCount(count);
    containersAvailableForLoading.acquire(count);
    LOGGER.debug("Reserved {} containers for loading", count);
  }

  public void cancelContainerReservation(int count) {
    validateCount(count);
    containersAvailableForLoading.release(count);
    LOGGER.debug("Cancelled reservation of {} container", count);
  }

  public void completeLoading(int count) {
    validateCount(count);
    int total = storedContainerCount.addAndGet(-count);
    freeContainerSlots.release(count);
    LOGGER.info("Loaded {} containers. Containers in port {}/{}", count, total, MAX_CONTAINER_COUNT);
  }

  public int getStoredContainerCount() {
    return storedContainerCount.get();
  }

  private void validateCount(int count) {
    if (count < 0 || count > MAX_CONTAINER_COUNT) {
      throw new IllegalArgumentException("Invalid container count" + count);
    }
  }
}
