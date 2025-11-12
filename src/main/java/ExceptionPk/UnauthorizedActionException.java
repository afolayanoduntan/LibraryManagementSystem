package ExceptionPk;

// Custom Exception for Unauthorized Action
public class UnauthorizedActionException extends Exception {
  public UnauthorizedActionException(String message) {
    super(message);
  }
}
