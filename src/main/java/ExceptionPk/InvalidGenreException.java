package ExceptionPk;

//Custom Exception for Invalid Genre
public class InvalidGenreException extends Exception {
  public InvalidGenreException(String message) {
    super(message);
  }
}
