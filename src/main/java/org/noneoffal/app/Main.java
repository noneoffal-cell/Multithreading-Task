package org.noneoffal.app;

import org.noneoffal.app.application.Application;

import java.io.IOException;
import java.io.UncheckedIOException;

public class Main {

  private Main() {
  }

  public static void main() {
    Application application = new Application();

    try {
      application.run();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Application was interrupted", e);
    } catch (IOException e) {
      throw new UncheckedIOException("Application failed", e);
    }
  }
}