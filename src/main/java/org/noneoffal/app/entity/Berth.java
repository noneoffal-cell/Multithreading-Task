package org.noneoffal.app.entity;

public record Berth(int id) {

  public Berth {
    if (id <= 0) {
      throw new IllegalArgumentException("Berth ID must be positive");
    }
  }

}