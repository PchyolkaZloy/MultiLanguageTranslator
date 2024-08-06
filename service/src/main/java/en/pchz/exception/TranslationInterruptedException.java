package en.pchz.exception;

public class TranslationInterruptedException extends TranslationThreadsExecutorServiceException {
    public TranslationInterruptedException(String message) {
        super(message);
    }

    public TranslationInterruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
