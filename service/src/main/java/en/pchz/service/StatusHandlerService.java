package en.pchz.service;

import en.pchz.exception.TranslationApiException;

public interface StatusHandlerService {
     void handleError(Integer statusCode, String message) throws TranslationApiException;
}
