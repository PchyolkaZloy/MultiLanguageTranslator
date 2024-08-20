package en.pchz.service.implementation;

import en.pchz.exception.TranslationApiException;
import en.pchz.service.abstraction.StatusHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HttpStatusHandlerService implements StatusHandlerService {
    private static final Logger log = LoggerFactory.getLogger(HttpStatusHandlerService.class);

    @Override
    public void handleError(Integer statusCode, String message) throws TranslationApiException {
        switch (statusCode) {
            case 200 -> log.debug("Request successful.");
            case 400 -> {
                log.error("Invalid argument provided: {}", message);
                throw new TranslationApiException("The source language was not found." +
                        " See /translator/languages for a list of available languages and their codes", statusCode);
            }
            case 401 -> {
                log.error("Unauthenticated request: {}", message);
                throw new TranslationApiException("Unauthenticated request", statusCode);
            }
            case 403 -> {
                log.error("Operation aborted. Permission denied: {}", message);
                throw new TranslationApiException("Permission denied", statusCode);
            }
            case 404 -> {
                log.warn("Requested resource not found: {}", message);
                throw new TranslationApiException("Resource not found", statusCode);
            }
            case 409 -> {
                log.warn("Resource already exists: {}", message);
                throw new TranslationApiException("Resource already exists", statusCode);
            }
            case 429 -> {
                log.error("Resource exhausted: {}", message);
                throw new TranslationApiException("Resource exhausted", statusCode);
            }
            case 499 -> {
                log.warn("Request was cancelled: {}", message);
                throw new TranslationApiException("Request was cancelled", statusCode);
            }
            case 500 -> {
                log.error("Internal server error: {}", message);
                throw new TranslationApiException("Internal server error", statusCode);
            }
            case 501 -> {
                log.error("Operation not implemented: {}", message);
                throw new TranslationApiException("Unimplemented request", statusCode);
            }
            case 503 -> {
                log.error("Service unavailable: {}", message);
                throw new TranslationApiException("Service unavailable", statusCode);
            }
            case 504 -> {
                log.error("Deadline exceeded: {}", message);
                throw new TranslationApiException("Deadline exceeded", statusCode);
            }
            default -> {
                log.error("Unhandled http status code: {}", message);
                throw new TranslationApiException("Unhandled http status code", statusCode);
            }
        }
    }
}