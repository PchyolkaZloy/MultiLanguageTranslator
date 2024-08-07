package en.pchz.service;

import en.pchz.common.Language;
import en.pchz.common.LanguageApiResponse;
import en.pchz.common.TranslationApiRequest;
import en.pchz.common.TranslationApiResponse;
import en.pchz.exception.RateLimitExceededException;
import en.pchz.exception.TranslationApiServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class YandexTranslationApiService implements TranslationApiService {
    private final String translateUrl;
    private final String languagesUrl;
    private final String key;
    private final RestTemplate restTemplate;
    private final StatusHandlerService statusHandlerService;
    private static final Logger log = LoggerFactory.getLogger(YandexTranslationApiService.class);

    @Autowired
    public YandexTranslationApiService(
            @Value("${translation.api.url.translate}") String translateUrl,
            @Value("${translation.api.url.languages}") String languagesUrl,
            @Value("${translation.api.key}") String key,
            StatusHandlerService statusHandlerService) {
        this.translateUrl = translateUrl;
        this.languagesUrl = languagesUrl;
        this.key = key;
        this.statusHandlerService = statusHandlerService;
        this.restTemplate = new RestTemplate();
    }

    public String makeTranslateRequest(String word, String sourceLanguage, String targetLanguage) {
        TranslationApiRequest requestBody = new TranslationApiRequest(sourceLanguage, targetLanguage, Collections.singletonList(word));
        HttpEntity<TranslationApiRequest> entity = new HttpEntity<>(requestBody, createHeaders());

        int maxRetries = 5;
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                ResponseEntity<TranslationApiResponse> response = restTemplate.exchange(
                        translateUrl,
                        HttpMethod.POST,
                        entity,
                        TranslationApiResponse.class);

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    TranslationApiResponse responseBody = response.getBody();
                    if (responseBody.translations() != null && !responseBody.translations().isEmpty()) {
                        return responseBody.translations().getFirst().text();
                    }
                }
            } catch (ResourceAccessException accessException) {
                log.error("Resource access failure to {}", translateUrl);
                statusHandlerService.handleError(500, String.format("Resource access failure to %s", translateUrl));
            } catch (HttpClientErrorException | HttpServerErrorException httpStatusCodeException) {
                if (httpStatusCodeException.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    retryCount++;
                    log.warn("Rate limit exceeded. Retrying... Attempt: {}/{}", retryCount, maxRetries);

                    if (retryCount < maxRetries) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException interruptedException) {
                            log.error("Thread interrupted during sleep", interruptedException);
                            throw new TranslationApiServiceException("Thread interrupted during sleep", interruptedException);
                        }
                    } else {
                        log.error("Max retry limit reached. API request error: {}", httpStatusCodeException.getResponseBodyAsString());
                        throw new RateLimitExceededException(
                                "Max retry limit reached. API request error: " + httpStatusCodeException.getResponseBodyAsString(),
                                httpStatusCodeException);
                    }
                } else {
                    statusHandlerService.handleError(
                            httpStatusCodeException.getStatusCode().value(),
                            httpStatusCodeException.getResponseBodyAsString());
                }
            }
        }
        return null;
    }

    @Override
    public List<Language> makeSupportLanguagesRequest() {
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders());

        try {
            ResponseEntity<LanguageApiResponse> response = restTemplate.exchange(
                    languagesUrl,
                    HttpMethod.POST,
                    entity,
                    LanguageApiResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Successfully retrieved supported languages");
                return response.getBody().languages();
            } else {
                log.error("Failed to retrieve supported languages. Unexpected response format.");
                statusHandlerService.handleError(response.getStatusCode().value(), "message");
            }
        } catch (ResourceAccessException accessException) {
            log.error("Resource access failure to {}", translateUrl);
            statusHandlerService.handleError(500, String.format("Resource access failure to %s", translateUrl));
        } catch (HttpClientErrorException | HttpServerErrorException httpStatusCodeException) {
            statusHandlerService.handleError(
                    httpStatusCodeException.getStatusCode().value(),
                    httpStatusCodeException.getResponseBodyAsString()
            );
        }
        return null;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Api-Key " + key);

        return headers;
    }
}