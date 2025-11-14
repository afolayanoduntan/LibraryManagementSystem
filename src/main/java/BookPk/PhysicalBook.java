package BookPk;

import ExceptionPk.BookNotFoundException;
import ExceptionPk.UnauthorizedActionException;
import GenrePk.Genre;

public class PhysicalBook extends Book implements Borrowable {

  // Constructor for new books
  public PhysicalBook(String title, String author, Genre genre, String addedBy) {
    super(title, author, genre, addedBy);
  }

  // Constructor for database-loaded books
  public PhysicalBook(Integer bookId, String title, String author, Genre genre, Boolean isBorrowed, String addedBy, String borrowedBy) {
    super(bookId, title, author, genre, isBorrowed, addedBy, borrowedBy);
  }

  @Override
  public void borrowBook(String userId) throws BookNotFoundException {
    if (!getIsBorrowed()) {
      setIsBorrowed(true);
      setBorrowedBy(userId);
      System.out.println(getTitle() + " has been borrowed by user " + userId);
    } else {
      throw new BookNotFoundException(getTitle() + " is already borrowed by user " + getBorrowedBy());
    }
  }

  @Override
  public void returnBook(String userId) throws BookNotFoundException, UnauthorizedActionException {
    if (getIsBorrowed()) {
      if (!userId.equals(getBorrowedBy()))
        throw new UnauthorizedActionException("Only user " + getBorrowedBy() + " can return this book");
      setIsBorrowed(false);
      setBorrowedBy(null);
      System.out.println(getTitle() + " has been returned by user " + userId);
    } else {
      throw new BookNotFoundException(getTitle() + " was not borrowed.");
    }
  }
}
