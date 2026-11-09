package org.noneoffal.app.entity;

public record ShipResult(
        int shipId,
        int initialContainers,
        int finalContainers,
        int unloadedContainers,
        int loadedContainers) {
}
