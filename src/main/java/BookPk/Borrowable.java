package BookPk;

import ExceptionPk.*;

public interface Borrowable {
  void borrowBook(String userId) throws BookNotFoundException, UserNotFoundException;
  void returnBook(String userId) throws BookNotFoundException, UserNotFoundException, UnauthorizedActionException;
}