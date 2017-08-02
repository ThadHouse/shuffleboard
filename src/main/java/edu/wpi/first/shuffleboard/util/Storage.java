package edu.wpi.first.shuffleboard.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utilities for local file storage.
 */
public final class Storage {

  /**
   * The user home directory.
   */
  private static final String USER_HOME = System.getProperty("user.home");

  private static final String STORAGE_DIR = USER_HOME + "/SmartDashboard";

  public static final String RECORDING_FILE_FORMAT = STORAGE_DIR + "/dashboard_recording_%s.frc";

  public static final String DEFAULT_RECORDING_FILE = STORAGE_DIR + "/default_dashboard_recording.frc";

  /**
   *
   * @return The root dashboard storage directory, ensuring it exists.
   * @throws IOException if the directory does not exist and can't be created
   */
  public static File getStorageDir() throws IOException {
    Path storage = Paths.get(STORAGE_DIR);
    Files.createDirectories(storage);
    return storage.toFile();
  }

  private Storage() {
  }

}
