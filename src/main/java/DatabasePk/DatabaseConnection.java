package DatabasePk;

import java.sql.*;
import java.io.*;

public class DatabaseConnection {
  private static final String URL;
  private static final String USERNAME;
  private static final String PASSWORD;

  private static Connection connection = null;

  // Static block to load configuration from .env file
  static {
    loadEnvConfig();

    // Set the values after loading from .env
    URL = getEnvValue("DB_URL", "jdbc:mysql://localhost:3306/library_management");
    USERNAME = getEnvValue("DB_USERNAME", "root");
    PASSWORD = getEnvValue("DB_PASSWORD", "");

  }

  private static void loadEnvConfig() {
    File envFile = new File(".env");

    if (!envFile.exists()) {
      System.out.println(".env file not found. Using default values or system environment variables.");
      System.out.println("Create a .env file from .env.template for custom configuration");
      return;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();

        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }

        String[] parts = line.split("=", 2);
        if (parts.length == 2) {
          String key = parts[0].trim();
          String value = parts[1].trim();

          // Remove quotes if present
          if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
          }

          System.setProperty(key, value);
        }
      }
      System.out.println("Loaded configuration from .env file");
    } catch (IOException e) {
      System.out.println("Error reading .env file: " + e.getMessage());
    }
  }

  private static String getEnvValue(String key, String defaultValue) {
    // First check system properties (from .env file)
    String value = System.getProperty(key);

    // If not found in .env, check system environment variables
    if (value == null) {
      value = System.getenv(key);
    }

    // If still not found, use default value
    return value != null ? value : defaultValue;
  }

  public static Connection getConnection() {
    if (connection == null) {
      try {
        connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        initializeDatabase(); // Initialize tables when first connecting
        System.out.println("Database connection established successfully!");
      } catch (SQLException e) {
        System.out.println("Database connection failed: " + e.getMessage());
        System.out.println("Please check your .env file configuration");
        System.out.println("Make sure your database is running and credentials are correct");
        throw new RuntimeException("Failed to connect to database", e);
      }
    }
    return connection;
  }

  private static void initializeDatabase() {
    try (Statement stmt = getConnection().createStatement()) {

      // Create users table
      String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    user_id VARCHAR(8) PRIMARY KEY,
                    username VARCHAR(100) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

      // Create books table with user tracking
      String createBooksTable = """
                CREATE TABLE IF NOT EXISTS books (
                    book_id INT PRIMARY KEY AUTO_INCREMENT,
                    title VARCHAR(255) NOT NULL,
                    author VARCHAR(255) NOT NULL,
                    genre VARCHAR(50) NOT NULL,
                    book_type ENUM('PHYSICAL', 'EBOOK') NOT NULL,
                    is_borrowed BOOLEAN DEFAULT FALSE,
                    added_by VARCHAR(8),
                    borrowed_by VARCHAR(8),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (added_by) REFERENCES users(user_id),
                    FOREIGN KEY (borrowed_by) REFERENCES users(user_id)
                )
                """;

      // Create borrowing_records table
      String createBorrowingRecordsTable = """
                CREATE TABLE IF NOT EXISTS borrowing_records (
                    record_id INT PRIMARY KEY AUTO_INCREMENT,
                    book_id INT NOT NULL,
                    user_id VARCHAR(8) NOT NULL,
                    borrowed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    due_date TIMESTAMP NOT NULL,
                    returned_at TIMESTAMP NULL,
                    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
                    FOREIGN KEY (user_id) REFERENCES users(user_id)
                )
                """;

      // Create activity_log table
      String createActivityLogTable = """
                CREATE TABLE IF NOT EXISTS activity_log (
                    log_id INT PRIMARY KEY AUTO_INCREMENT,
                    activity_description TEXT NOT NULL,
                    user_id VARCHAR(8),
                    activity_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(user_id)
                )
                """;

      // Execute all table creation statements
      stmt.execute(createUsersTable);
      stmt.execute(createBooksTable);
      stmt.execute(createBorrowingRecordsTable);
      stmt.execute(createActivityLogTable);

      System.out.println("Database tables initialized successfully!");

    } catch (SQLException e) {
      System.out.println("Error initializing database tables: " + e.getMessage());
      // Don't throw exception - the app might still work with existing tables
    }
  }

  public static void closeConnection() {
    if (connection != null) {
      try {
        connection.close();
        connection = null;
        System.out.println("Database connection closed.");
      } catch (SQLException e) {
        System.out.println("Error closing connection: " + e.getMessage());
      }
    }
  }

  //Test connection method
  public static boolean testConnection() {
    try (Connection testConn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
      System.out.println("Database connection test: SUCCESS");
      return true;
    } catch (SQLException e) {
      System.out.println("Database connection test: FAILED");
      System.out.println("Error: " + e.getMessage());
      return false;
    }
  }
}

