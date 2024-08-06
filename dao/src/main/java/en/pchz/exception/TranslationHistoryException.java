package en.pchz.exception;

public class TranslationHistoryException extends RuntimeException {
    public TranslationHistoryException(String message) {
        super(message);
    }

    public TranslationHistoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
