package en.pchz.exception;

public class TranslationThreadsExecutorServiceException extends RuntimeException {
    public TranslationThreadsExecutorServiceException(String message) {
        super(message);
    }

    public TranslationThreadsExecutorServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}