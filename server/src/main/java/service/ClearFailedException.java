package service;

/**
 * Indicates there was an error clearing the database
 */
public class ClearFailedException extends RuntimeException {
    public ClearFailedException(String message) {
      super(message);
    }
    public ClearFailedException(String message, Throwable ex) {
      super(message, ex);
  }
}
