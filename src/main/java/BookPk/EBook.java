package BookPk;

import ExceptionPk.BookNotFoundException;
import GenrePk.Genre;

public class EBook extends Book implements Borrowable {
  public EBook(String title, String author, Genre genre, String addedBy) {
    super(title, author, genre, addedBy);
  }

  public EBook(Integer bookId, String title, String author, Genre genre, Boolean isBorrowed, String addedBy, String borrowedBy) {
    super(bookId, title, author, genre, isBorrowed, addedBy, borrowedBy);
  }

  @Override
  public void borrowBook(String userId) throws BookNotFoundException {
    System.out.println(getTitle() + " (E-Book) has been downloaded by user " + userId);
  }

  @Override
  public void returnBook(String userId) throws BookNotFoundException {
    System.out.println(getTitle() + " (E-Book) does not need to be returned");
  }
}
