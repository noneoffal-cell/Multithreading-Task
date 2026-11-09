package org.noneoffal.app.resources;

import org.noneoffal.app.entity.Berth;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class BerthManager {

  private final Deque<Berth> availableBerths;
  private final Semaphore availablePermits;
  private final ReentrantLock lock;

  public BerthManager(int berthCount) {
    this.availableBerths = new ArrayDeque<>();
    this.availablePermits = new Semaphore(berthCount, true);
    this.lock = new ReentrantLock();

    for (int i = 1; i <= berthCount; i++) {
      availableBerths.addLast(new Berth(i));
    }
  }

  public Berth acquireBerth() throws InterruptedException {
    availablePermits.acquire();

    lock.lock();
    try {
      return availableBerths.removeFirst();
    } finally {
      lock.unlock();
    }
  }

  public void releaseBerth(Berth berth) {
    lock.lock();
    try {
      availableBerths.addLast(berth);
    } finally {
      lock.unlock();
    }

    availablePermits.release();
  }


}
