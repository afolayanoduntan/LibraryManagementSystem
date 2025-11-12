package ExceptionPk;

//Custom Exception for Book Not Found
public class BookNotFoundException extends Exception {
  public BookNotFoundException(String message) {
    super(message);
  }
}
