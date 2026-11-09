package org.noneoffal.app.config;

public record ShipConfiguration(
        int id,
        int capacity,
        int initialContainers,
        int containersToUnload,
        int containersToLoad) {
}
