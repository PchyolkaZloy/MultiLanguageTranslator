package en.pchz.exception;

public class TranslationApiServiceException extends RuntimeException {
    public TranslationApiServiceException(String message) {
        super(message);
    }

    public TranslationApiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
