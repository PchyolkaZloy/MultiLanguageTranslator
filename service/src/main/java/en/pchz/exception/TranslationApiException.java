package en.pchz.exception;

public class TranslationApiException extends RuntimeException {
    private final int statusCode;

    public TranslationApiException(String message, int grpcCode) {
        super(message);
        this.statusCode = grpcCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
