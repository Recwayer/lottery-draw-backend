package ru.lottery.support;

import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

public class TestcontainersLauncherListener implements LauncherSessionListener {

  @Override
  public void launcherSessionOpened(LauncherSession session) {
    PostgresContainer.boot();
  }
}
