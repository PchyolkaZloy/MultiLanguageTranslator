package en.pchz.exception;

public class RateLimitExceededException extends TranslationApiServiceException {
    public RateLimitExceededException(String message) {
        super(message);
    }

    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
