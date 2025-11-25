import BookPk.*;
import DatabasePk.*;
import ExceptionPk.*;
import GenrePk.Genre;
import UserPk.User;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.*;
import java.util.UUID;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LibraryDatabaseTest {
  private Library library;
  private Connection connection;

  @BeforeAll
  public void setUp() throws SQLException {
    library = new Library();
    connection = DatabaseConnection.getConnection();
  }

  @BeforeEach
  public void beginTransaction() throws SQLException {
    connection.setAutoCommit(false);
  }

  @AfterEach
  public void rollbackTransaction() throws SQLException {
    connection.rollback();
    connection.setAutoCommit(true);
  }

  @AfterAll
  public void tearDown() throws SQLException {
    if (connection != null && !connection.isClosed()) {
      connection.close();
    }
  }

  private String generateUniqueUserId() {
    return String.format("%08d", Math.abs(UUID.randomUUID().hashCode() % 100000000));
  }

  private String generateUniqueBookTitle(String prefix) {
    return prefix + "_" + UUID.randomUUID().toString().substring(0, 12);
  }

  @Test
  public void testAddBookToDatabase() throws Exception {
    String uniqueUserId = generateUniqueUserId();
    String uniqueBookTitle = generateUniqueBookTitle("PhysicalBook");

    library.addUser(new User(uniqueUserId, "Test User"));
    PhysicalBook book = new PhysicalBook(uniqueBookTitle, "Test Author", Genre.FICTION, uniqueUserId);

    library.addBook(book);
    int bookId = book.getBookId();

    assertTrue(bookId > 0, "Book should have database-generated ID");

    // Verify by trying to borrow the book (this will fail if book doesn't exist)
    assertDoesNotThrow(() -> library.borrowBook(bookId, uniqueUserId));
  }

  @Test
  public void testAddEBookToDatabase() throws Exception {
    String uniqueUserId = generateUniqueUserId();
    String uniqueBookTitle = generateUniqueBookTitle("EBook");

    library.addUser(new User(uniqueUserId, "EBook User"));
    EBook ebook = new EBook(uniqueBookTitle, "E-Author", Genre.TECHNOLOGY, uniqueUserId);

    library.addBook(ebook);
    int bookId = ebook.getBookId();

    assertTrue(bookId > 0, "E-Book should have database-generated ID");

    assertDoesNotThrow(() -> library.displayBooks());
  }

  @Test
  public void testBorrowBookDatabaseIntegration() throws Exception {
    String uniqueUserId = generateUniqueUserId();
    String uniqueBookTitle = generateUniqueBookTitle("BorrowTest");

    library.addUser(new User(uniqueUserId, "Borrower"));
    PhysicalBook book = new PhysicalBook(uniqueBookTitle, "Author", Genre.SCIENCE, uniqueUserId);
    library.addBook(book);
    int bookId = book.getBookId();

    // Borrow should succeed for available book
    assertDoesNotThrow(() -> library.borrowBook(bookId, uniqueUserId));

    // Try to borrow again - should fail
    assertThrows(BookNotFoundException.class, () -> {
      library.borrowBook(bookId, uniqueUserId);
    });
  }

  @Test
  public void testReturnBookDatabaseIntegration() throws Exception {
    String uniqueUserId = generateUniqueUserId();
    String uniqueBookTitle = generateUniqueBookTitle("ReturnTest");

    library.addUser(new User(uniqueUserId, "Returner"));
    PhysicalBook book = new PhysicalBook(uniqueBookTitle, "Author", Genre.HISTORY, uniqueUserId);
    library.addBook(book);
    int bookId = book.getBookId();

    // Borrow the book first
    library.borrowBook(bookId, uniqueUserId);

    // Return should succeed
    assertDoesNotThrow(() -> library.returnBook(bookId, uniqueUserId));

    // Book should be available for borrowing again
    assertDoesNotThrow(() -> library.borrowBook(bookId, uniqueUserId));
  }

  @Test
  public void testRemoveBookFromDatabase() throws Exception {
    String uniqueUserId = generateUniqueUserId();
    String uniqueBookTitle = generateUniqueBookTitle("RemoveTest");

    library.addUser(new User(uniqueUserId, "Remover"));
    PhysicalBook book = new PhysicalBook(uniqueBookTitle, "Author", Genre.NON_FICTION, uniqueUserId);
    library.addBook(book);
    int bookId = book.getBookId();

    assertDoesNotThrow(() -> library.removeBook(bookId, uniqueUserId));

    assertThrows(BookNotFoundException.class, () -> {
      library.borrowBook(bookId, uniqueUserId);
    });
  }

  @Test
  public void testUserManagementDatabaseIntegration() throws Exception {
    String uniqueUserId = generateUniqueUserId();
    User user = new User(uniqueUserId, "Database User");

    library.addUser(user);

    // Test user exists
    assertTrue(library.userExists(uniqueUserId), "User should exist");

    // Test retrieving the user
    User retrievedUser = library.getUser(uniqueUserId);
    assertNotNull(retrievedUser, "Should retrieve the user");
    assertEquals("Database User", retrievedUser.getUsername());
    assertEquals(uniqueUserId, retrievedUser.getUserId());
  }

  @Test
  public void testDisplayBooksFromDatabase() throws Exception {
    String uniqueUserId = generateUniqueUserId();

    library.addUser(new User(uniqueUserId, "Display Tester"));

    String book1Title = generateUniqueBookTitle("Display1");
    String book2Title = generateUniqueBookTitle("Display2");
    String ebookTitle = generateUniqueBookTitle("DisplayE");

    PhysicalBook book1 = new PhysicalBook(book1Title, "Author 1", Genre.FICTION, uniqueUserId);
    PhysicalBook book2 = new PhysicalBook(book2Title, "Author 2", Genre.SCIENCE, uniqueUserId);
    EBook ebook1 = new EBook(ebookTitle, "E-Author 1", Genre.TECHNOLOGY, uniqueUserId);

    library.addBook(book1);
    library.addBook(book2);
    library.addBook(ebook1);

    assertDoesNotThrow(() -> library.displayBooks());
  }

  @Test
  public void testOverdueBooksFunctionality() throws Exception {
    String uniqueUserId = generateUniqueUserId();
    String uniqueBookTitle = generateUniqueBookTitle("OverdueTest");

    library.addUser(new User(uniqueUserId, "Overdue User"));
    PhysicalBook book = new PhysicalBook(uniqueBookTitle, "Author", Genre.FICTION, uniqueUserId);
    library.addBook(book);
    library.borrowBook(book.getBookId(), uniqueUserId);

    assertDoesNotThrow(() -> library.displayOverdueBooks());
  }

  @Test
  public void testBorrowingHistoryFunctionality() throws Exception {
    String uniqueUserId = generateUniqueUserId();
    String uniqueBookTitle = generateUniqueBookTitle("HistoryTest");

    library.addUser(new User(uniqueUserId, "History User"));
    PhysicalBook book = new PhysicalBook(uniqueBookTitle, "Author", Genre.FICTION, uniqueUserId);
    library.addBook(book);
    library.borrowBook(book.getBookId(), uniqueUserId);
    library.returnBook(book.getBookId(), uniqueUserId);

    assertDoesNotThrow(() -> library.displayBorrowingHistory());
  }

  @Test
  public void testUnauthorizedBookRemoval() throws Exception {
    String user1Id = generateUniqueUserId();
    String user2Id = generateUniqueUserId();
    String uniqueBookTitle = generateUniqueBookTitle("AuthTest");

    library.addUser(new User(user1Id, "Owner"));
    library.addUser(new User(user2Id, "Other User"));

    PhysicalBook book = new PhysicalBook(uniqueBookTitle, "Author", Genre.FICTION, user1Id);
    library.addBook(book);
    int bookId = book.getBookId();

    assertThrows(UnauthorizedActionException.class, () -> {
      library.removeBook(bookId, user2Id);
    });
  }

  @Test
  public void testUnauthorizedBookReturn() throws Exception {
    String user1Id = generateUniqueUserId();
    String user2Id = generateUniqueUserId();
    String uniqueBookTitle = generateUniqueBookTitle("ReturnAuthTest");

    library.addUser(new User(user1Id, "Borrower"));
    library.addUser(new User(user2Id, "Other User"));

    PhysicalBook book = new PhysicalBook(uniqueBookTitle, "Author", Genre.FICTION, user1Id);
    library.addBook(book);
    int bookId = book.getBookId();
    library.borrowBook(bookId, user1Id);

    assertThrows(UnauthorizedActionException.class, () -> {
      library.returnBook(bookId, user2Id);
    });
  }

  @Test
  public void testDoubleBorrowPrevention() throws Exception {
    String user1Id = generateUniqueUserId();
    String user2Id = generateUniqueUserId();
    String uniqueBookTitle = generateUniqueBookTitle("DoubleBorrowTest");

    library.addUser(new User(user1Id, "First Borrower"));
    library.addUser(new User(user2Id, "Second Borrower"));

    PhysicalBook book = new PhysicalBook(uniqueBookTitle, "Author", Genre.FICTION, user1Id);
    library.addBook(book);
    int bookId = book.getBookId();
    library.borrowBook(bookId, user1Id);

    assertThrows(BookNotFoundException.class, () -> {
      library.borrowBook(bookId, user2Id);
    });
  }

  @Test
  public void testUserCountIncrease() throws Exception {
    int initialCount = library.getUserCount();
    String uniqueUserId = generateUniqueUserId();

    library.addUser(new User(uniqueUserId, "New Test User"));

    int finalCount = library.getUserCount();
    assertEquals(initialCount + 1, finalCount, "User count should increase by 1");

    // Also test the specific user exists
    assertTrue(library.userExists(uniqueUserId));
  }

  @Test
  public void testBookCountIncrease() throws Exception {
    String uniqueUserId = generateUniqueUserId();
    library.addUser(new User(uniqueUserId, "Count Tester"));

    int initialCount = library.getBookCount();

    PhysicalBook book1 = new PhysicalBook("TestBook1", "Author1", Genre.FICTION, uniqueUserId);
    PhysicalBook book2 = new PhysicalBook("TestBook2", "Author2", Genre.SCIENCE, uniqueUserId);

    library.addBook(book1);
    library.addBook(book2);

    int finalCount = library.getBookCount();
    assertEquals(initialCount + 2, finalCount, "Book count should increase by 2");
  }

  @Test
  public void testBookTypeOperations() throws Exception {
    String uniqueUserId = generateUniqueUserId();
    library.addUser(new User(uniqueUserId, "Type Tester"));

    PhysicalBook physicalBook = new PhysicalBook("Physical Test", "Author", Genre.FICTION, uniqueUserId);
    EBook ebook = new EBook("Ebook Test", "E-Author", Genre.TECHNOLOGY, uniqueUserId);

    library.addBook(physicalBook);
    library.addBook(ebook);

    // Test that both books can be processed without errors
    assertDoesNotThrow(() -> library.displayBooks());

    assertDoesNotThrow(() -> library.borrowBook(physicalBook.getBookId(), uniqueUserId));
    assertDoesNotThrow(() -> library.returnBook(physicalBook.getBookId(), uniqueUserId));

    assertDoesNotThrow(() -> library.borrowBook(ebook.getBookId(), uniqueUserId));
  }

  @Test
  public void testUserOperations() throws Exception {
    String uniqueUserId = generateUniqueUserId();
    User user = new User(uniqueUserId, "Test User");

    // Add user
    library.addUser(user);
    assertTrue(library.userExists(uniqueUserId));

    // Retrieve user
    User retrievedUser = library.getUser(uniqueUserId);
    assertNotNull(retrievedUser);
    assertEquals(uniqueUserId, retrievedUser.getUserId());
    assertEquals("Test User", retrievedUser.getUsername());

    assertDoesNotThrow(() -> library.displayAllUsers());
  }

  @Test
  public void testNonExistentBookOperations() throws Exception {
    String uniqueUserId = generateUniqueUserId();
    library.addUser(new User(uniqueUserId, "Test User"));

    assertThrows(BookNotFoundException.class, () -> {
      library.borrowBook(99999, uniqueUserId);
    });

    assertThrows(BookNotFoundException.class, () -> {
      library.returnBook(99999, uniqueUserId);
    });

    assertThrows(BookNotFoundException.class, () -> {
      library.removeBook(99999, uniqueUserId);
    });
  }

  @Test
  public void testNonExistentUserOperations() throws Exception {
    // First, create a real user and book for testing
    String uniqueUserId = generateUniqueUserId();
    library.addUser(new User(uniqueUserId, "Test User"));

    String uniqueBookTitle = generateUniqueBookTitle("TestBook");
    PhysicalBook book = new PhysicalBook(uniqueBookTitle, "Author", Genre.FICTION, uniqueUserId);
    library.addBook(book);
    int bookId = book.getBookId();

    // Now test with a non-existent user ID
    String nonExistentUserId = "00000000";

    assertThrows(UserNotFoundException.class, () -> {
      library.borrowBook(bookId, nonExistentUserId);
    });


  }
}