package ExceptionPk;

// Custom Exception for User.User Not Found
public class UserNotFoundException extends Exception {
  public UserNotFoundException(String message) {
    super(message);
  }
}