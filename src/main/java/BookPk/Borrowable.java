package BookPk;

import ExceptionPk.BookNotFoundException;
import ExceptionPk.UnauthorizedActionException;
import ExceptionPk.UserNotFoundException;

public interface Borrowable {
  void borrowBook(String userId) throws BookNotFoundException, UserNotFoundException;
  void returnBook(String userId) throws BookNotFoundException, UserNotFoundException, UnauthorizedActionException;
}