package DatabasePk;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class EnvironmentLoader {
  private static Map<String, String> envVariables = new HashMap<>();

  static {
    loadEnvFile();
  }

  private static void loadEnvFile() {
    File envFile = new File(".env");

    if (!envFile.exists()) {
      System.out.println("Warning: .env file not found. Using system environment variables.");
      return;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();

        // Skip empty lines and comments
        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }

        // Split key and value
        String[] parts = line.split("=", 2);
        if (parts.length == 2) {
          String key = parts[0].trim();
          String value = parts[1].trim();

          // Remove quotes if present
          if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
          }

          envVariables.put(key, value);
          System.setProperty(key, value); // Also set as system property
        }
      }
      System.out.println("Loaded environment variables from .env file");
    } catch (IOException e) {
      System.out.println("Error reading .env file: " + e.getMessage());
    }
  }

  public static String get(String key) {
    // First check .env file, then system environment variables
    String value = envVariables.get(key);
    if (value == null) {
      value = System.getenv(key);
    }
    if (value == null) {
      value = System.getProperty(key);
    }
    return value;
  }

  public static String get(String key, String defaultValue) {
    String value = get(key);
    return value != null ? value : defaultValue;
  }

  public static void printAll() {
    System.out.println("=== Environment Variables ===");
    envVariables.forEach((key, value) -> {
      System.out.println(key + " = " + (key.contains("PASSWORD") ? "***" : value));
    });
  }
}

