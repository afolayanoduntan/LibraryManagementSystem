package BookPk;

import DatabasePk.DatabaseConnection;
import ExceptionPk.*;
import GenrePk.Genre;
import UserPk.User;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Library {
  private Connection connection;
  private HashMap<Integer, Book> bookCache = new HashMap<>();
  private HashMap<String, User> userCache = new HashMap<>();
  private ArrayList<Book> allBooksList = new ArrayList<>();
  private LinkedList<String> recentActivities = new LinkedList<>();

  public Library() {
    this.connection = DatabaseConnection.getConnection();
    loadDataIntoMemory();
  }

  private void loadDataIntoMemory() {
    loadAllBooksIntoMemory();
    loadAllUsersIntoMemory();
    System.out.println("Loaded " + bookCache.size() + " books and " + userCache.size() + " users");
  }

  private void loadAllBooksIntoMemory() {
    String sql = "SELECT * FROM books";
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        Book book = createBookFromResultSet(rs);
        if (book != null) {
          bookCache.put(book.getBookId(), book);
          allBooksList.add(book);
        }
      }
    } catch (SQLException e) {
      System.out.println("Error loading books: " + e.getMessage());
    }
  }

  private void loadAllUsersIntoMemory() {
    String sql = "SELECT * FROM users";
    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        User user = new User(rs.getString("user_id"), rs.getString("username"));
        userCache.put(user.getUserId(), user);
      }
    } catch (SQLException e) {
      System.out.println("Error loading users: " + e.getMessage());
    }
  }

  public void addUser(User user) {
    userCache.put(user.getUserId(), user);

    String sql = "INSERT INTO users (user_id, username) VALUES (?, ?)";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setString(1, user.getUserId());
      pstmt.setString(2, user.getUsername());
      pstmt.executeUpdate();
      System.out.println("User added successfully: " + user.getUsername());
    } catch (SQLException e) {
      System.out.println("Error adding user: " + e.getMessage());
    }
  }

  public boolean userExists(String userId) {
    if (userCache.containsKey(userId)) {
      return true;
    }

    String sql = "SELECT 1 FROM users WHERE user_id = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setString(1, userId);
      try (ResultSet rs = pstmt.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      System.out.println("Error checking user existence: " + e.getMessage());
      return false;
    }
  }

  public User getUser(String userId) {
    User user = userCache.get(userId);
    if (user != null) {
      return user;
    }

    String sql = "SELECT * FROM users WHERE user_id = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setString(1, userId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return new User(rs.getString("user_id"), rs.getString("username"));
        }
      }
    } catch (SQLException e) {
      System.out.println("Error retrieving user: " + e.getMessage());
    }
    return null;
  }

  public void displayAllUsers() {
    System.out.println("\n=== REGISTERED USERS ===");
    if (userCache.isEmpty()) {
      System.out.println("No users registered in the system.");
    } else {
      for (User user : userCache.values()) {
        user.displayUser();
      }
    }
  }

  public void addBook(Book book) {
    String sql = "INSERT INTO books (title, author, genre, book_type, is_borrowed, added_by) VALUES (?, ?, ?, ?, ?, ?)";

    try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      pstmt.setString(1, book.getTitle());
      pstmt.setString(2, book.getAuthor());
      pstmt.setString(3, book.getGenre().name());
      pstmt.setString(4, (book instanceof PhysicalBook) ? "PHYSICAL" : "EBOOK");
      pstmt.setBoolean(5, book.getIsBorrowed());
      pstmt.setString(6, book.getAddedBy());

      int affectedRows = pstmt.executeUpdate();

      if (affectedRows > 0) {
        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            int generatedId = generatedKeys.getInt(1);
            book.setBookId(generatedId);

            bookCache.put(generatedId, book);
            allBooksList.add(book);
          }
        }

        logActivity("Added Book: " + book.getTitle() + " (ID: " + book.getBookId() + ")", book.getAddedBy());
        System.out.println("Book added successfully to database with ID: " + book.getBookId());
      }

    } catch (SQLException e) {
      System.out.println("Error adding book to database: " + e.getMessage());
    }
  }
  public Book findBookById(Integer bookId) {
    Book book = bookCache.get(bookId);
    if (book != null) {
      return book;
    }

    for (Book b : allBooksList) {
      if (b.getBookId().equals(bookId)) {
        return b;
      }
    }

    return findBookInDatabase(bookId);
  }

  private Book findBookInDatabase(Integer bookId) {
    String sql = "SELECT * FROM books WHERE book_id = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setInt(1, bookId);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          Book book = createBookFromResultSet(rs);
          if (book != null) {
            bookCache.put(bookId, book);
            allBooksList.add(book);
          }
          return book;
        }
      }
    } catch (SQLException e) {
      System.out.println("Error finding book in database: " + e.getMessage());
    }
    return null;
  }

  public void borrowBook(Integer bookId, String userId) throws BookNotFoundException, UserNotFoundException, UnauthorizedActionException {
    if (!userExists(userId)) {
      throw new UserNotFoundException("User ID " + userId + " not found.");
    }

    Book book = bookCache.get(bookId);
    if (book != null && book.getIsBorrowed()) {
      throw new BookNotFoundException("Book with ID " + bookId + " is already borrowed.");
    }

    String updateBookSql = "UPDATE books SET is_borrowed = TRUE, borrowed_by = ? WHERE book_id = ? AND is_borrowed = FALSE";
    try (PreparedStatement pstmt = connection.prepareStatement(updateBookSql)) {
      pstmt.setString(1, userId);
      pstmt.setInt(2, bookId);
      int rowsAffected = pstmt.executeUpdate();

      if (rowsAffected > 0) {
        if (book != null) {
          book.setIsBorrowed(true);
          book.setBorrowedBy(userId);
        }

        String insertRecordSql = "INSERT INTO borrowing_records (book_id, user_id, due_date) VALUES (?, ?, DATE_ADD(NOW(), INTERVAL 14 DAY))";
        try (PreparedStatement recordStmt = connection.prepareStatement(insertRecordSql)) {
          recordStmt.setInt(1, bookId);
          recordStmt.setString(2, userId);
          recordStmt.executeUpdate();
        }

        logActivity("Borrowed Book ID: " + bookId, userId);
        System.out.println("Book with ID " + bookId + " has been borrowed by user " + userId + ". Due in 14 days.");
      } else {
        if (bookExists(bookId)) {
          throw new BookNotFoundException("Book with ID " + bookId + " is already borrowed.");
        } else {
          throw new BookNotFoundException("Book with ID " + bookId + " not found.");
        }
      }
    } catch (SQLException e) {
      System.out.println("Error borrowing book: " + e.getMessage());
    }
  }

  public void returnBook(Integer bookId, String userId) throws BookNotFoundException, UserNotFoundException, UnauthorizedActionException {
    if (!userExists(userId)) {
      throw new UserNotFoundException("User ID " + userId + " not found.");
    }

    String checkSql = "SELECT borrowed_by FROM books WHERE book_id = ? AND is_borrowed = TRUE";
    try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
      checkStmt.setInt(1, bookId);
      ResultSet rs = checkStmt.executeQuery();

      if (rs.next()) {
        String borrowedBy = rs.getString("borrowed_by");
        if (!userId.equals(borrowedBy)) {
          throw new UnauthorizedActionException("Only user " + borrowedBy + " can return this book.");
        }
      } else {
        throw new BookNotFoundException("Book with ID " + bookId + " is not currently borrowed.");
      }
    } catch (SQLException e) {
      throw new BookNotFoundException("Error checking book status: " + e.getMessage());
    }

    String updateBookSql = "UPDATE books SET is_borrowed = FALSE, borrowed_by = NULL WHERE book_id = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(updateBookSql)) {
      pstmt.setInt(1, bookId);
      int rowsAffected = pstmt.executeUpdate();

      if (rowsAffected > 0) {
        Book book = bookCache.get(bookId);
        if (book != null) {
          book.setIsBorrowed(false);
          book.setBorrowedBy(null);
        }

        String updateRecordSql = "UPDATE borrowing_records SET returned_at = NOW() WHERE book_id = ? AND user_id = ? AND returned_at IS NULL";
        try (PreparedStatement recordStmt = connection.prepareStatement(updateRecordSql)) {
          recordStmt.setInt(1, bookId);
          recordStmt.setString(2, userId);
          recordStmt.executeUpdate();
        }

        logActivity("Returned Book ID: " + bookId, userId);
        System.out.println("Book with ID " + bookId + " has been returned by user " + userId + ".");
      } else {
        throw new BookNotFoundException("Book with ID " + bookId + " not found.");
      }
    } catch (SQLException e) {
      System.out.println("Error returning book: " + e.getMessage());
    }
  }

  public void removeBook(Integer bookId, String userId) throws BookNotFoundException, UnauthorizedActionException {
    String checkSql = "SELECT added_by FROM books WHERE book_id = ?";
    try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
      checkStmt.setInt(1, bookId);
      ResultSet rs = checkStmt.executeQuery();

      if (rs.next()) {
        String addedBy = rs.getString("added_by");
        if (!userId.equals(addedBy)) {
          throw new UnauthorizedActionException("Only user " + addedBy + " can remove this book.");
        }
      } else {
        throw new BookNotFoundException("Book with ID " + bookId + " not found.");
      }
    } catch (SQLException e) {
      throw new BookNotFoundException("Error checking book: " + e.getMessage());
    }

    String sql = "DELETE FROM books WHERE book_id = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setInt(1, bookId);
      int rowsAffected = pstmt.executeUpdate();

      if (rowsAffected > 0) {
        bookCache.remove(bookId);
        allBooksList.removeIf(book -> book.getBookId().equals(bookId));

        logActivity("Removed Book ID: " + bookId, userId);
        System.out.println("Book with ID " + bookId + " has been removed from database by user " + userId + ".");
      } else {
        throw new BookNotFoundException("Book with ID " + bookId + " not found.");
      }
    } catch (SQLException e) {
      System.out.println("Error removing book: " + e.getMessage());
    }
  }

  public void displayBooks() {
    System.out.println("\n=== LIBRARY BOOKS ===");
    if (allBooksList.isEmpty()) {
      System.out.println("No books available in the library.");
    } else {
      for (Book book : allBooksList) {
        // Show book type in the display
        String bookType = (book instanceof PhysicalBook) ? "Physical Book" : "E-Book";
        System.out.printf("ID: %d | %s by %s | Genre: %s | Type: %s | Status: %s | Added by: %s%n",
            book.getBookId(),
            book.getTitle(),
            book.getAuthor(),
            book.getGenre(),
            bookType,
            book.getIsBorrowed() ? "Borrowed by " + book.getBorrowedBy() : "Available",
            book.getAddedBy());
      }
    }
  }

  public void displayOverdueBooks() {
    String sql = """
            SELECT br.record_id, b.title, b.book_id, u.user_id, u.username, br.borrowed_at, br.due_date 
            FROM borrowing_records br
            JOIN books b ON br.book_id = b.book_id
            JOIN users u ON br.user_id = u.user_id
            WHERE br.returned_at IS NULL AND br.due_date < NOW()
            ORDER BY br.due_date
            """;

    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      System.out.println("\n=== OVERDUE BOOKS ===");
      boolean hasOverdue = false;
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

      while (rs.next()) {
        hasOverdue = true;
        System.out.printf("Book: %s (ID: %d) | User: %s (%s) | Due: %s | Status: OVERDUE%n",
            rs.getString("title"),
            rs.getInt("book_id"),
            rs.getString("username"),
            rs.getString("user_id"),
            rs.getTimestamp("due_date").toLocalDateTime().format(formatter));
      }

      if (!hasOverdue) {
        System.out.println("No overdue books.");
      }
    } catch (SQLException e) {
      System.out.println("Error retrieving overdue books: " + e.getMessage());
    }
  }

  public void displayBorrowingHistory() {
    String sql = """
            SELECT b.title, u.username, br.borrowed_at, br.due_date, br.returned_at
            FROM borrowing_records br
            JOIN books b ON br.book_id = b.book_id
            JOIN users u ON br.user_id = u.user_id
            ORDER BY br.borrowed_at DESC
            LIMIT 20
            """;

    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      System.out.println("\n=== RECENT BORROWING HISTORY ===");
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

      while (rs.next()) {
        String returnedAt = rs.getTimestamp("returned_at") != null ?
            rs.getTimestamp("returned_at").toLocalDateTime().format(formatter) : "Not returned";

        System.out.printf("Book: %s | User: %s | Borrowed: %s | Due: %s | Returned: %s%n",
            rs.getString("title"),
            rs.getString("username"),
            rs.getTimestamp("borrowed_at").toLocalDateTime().format(formatter),
            rs.getTimestamp("due_date").toLocalDateTime().format(formatter),
            returnedAt);
      }
    } catch (SQLException e) {
      System.out.println("Error retrieving borrowing history: " + e.getMessage());
    }
  }

  public void viewActivityLog() {
    String sql = """
            SELECT al.activity_description, al.activity_date, u.username 
            FROM activity_log al 
            LEFT JOIN users u ON al.user_id = u.user_id 
            ORDER BY al.activity_date DESC 
            LIMIT 20
            """;

    try (Statement stmt = connection.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      System.out.println("\n=== RECENT ACTIVITY LOG ===");
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

      while (rs.next()) {
        String username = rs.getString("username") != null ?
            rs.getString("username") : "System";

        System.out.printf("[%s] %s - %s%n",
            rs.getTimestamp("activity_date").toLocalDateTime().format(formatter),
            username,
            rs.getString("activity_description"));
      }
    } catch (SQLException e) {
      System.out.println("Error retrieving activity log: " + e.getMessage());
    }
  }

  private void logActivity(String description, String userId) {
    recentActivities.addFirst("User " + userId + ": " + description);
    if (recentActivities.size() > 50) {
      recentActivities.removeLast();
    }

    String sql = "INSERT INTO activity_log (activity_description, user_id) VALUES (?, ?)";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setString(1, description);
      pstmt.setString(2, userId);
      pstmt.executeUpdate();
    } catch (SQLException e) {
      System.out.println("Error logging activity: " + e.getMessage());
    }
  }

  private boolean bookExists(Integer bookId) {
    if (bookCache.containsKey(bookId)) {
      return true;
    }

    String sql = "SELECT 1 FROM books WHERE book_id = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
      pstmt.setInt(1, bookId);
      try (ResultSet rs = pstmt.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      return false;
    }
  }

  private Book createBookFromResultSet(ResultSet rs) throws SQLException {
    try {
      Genre genre = Genre.valueOf(rs.getString("genre"));
      Book book;

      if ("PHYSICAL".equals(rs.getString("book_type"))) {
        book = new PhysicalBook(
            rs.getInt("book_id"),
            rs.getString("title"),
            rs.getString("author"),
            genre,
            rs.getBoolean("is_borrowed"),
            rs.getString("added_by"),
            rs.getString("borrowed_by")
        );
      } else {
        book = new EBook(
            rs.getInt("book_id"),
            rs.getString("title"),
            rs.getString("author"),
            genre,
            rs.getBoolean("is_borrowed"),
            rs.getString("added_by"),
            rs.getString("borrowed_by")
        );
      }
      return book;
    } catch (Exception e) {
      System.out.println("Error creating book from result set: " + e.getMessage());
      return null;
    }
  }

  public int getBookCount() {
    return bookCache.size();
  }

  public int getUserCount() {
    return userCache.size();
  }
}
