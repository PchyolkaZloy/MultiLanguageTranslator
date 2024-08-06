package en.pchz.service;

import en.pchz.common.Language;
import en.pchz.common.LanguageApiResponse;
import en.pchz.common.TranslationApiRequest;
import en.pchz.common.TranslationApiResponse;
import en.pchz.exception.RateLimitExceededException;
import en.pchz.exception.TranslationApiServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class YandexTranslationApiService implements TranslationApiService {
    private final String translateUrl;
    private final String languagesUrl;
    private final String key;
    private final RestTemplate restTemplate;
    private static final Logger log = LoggerFactory.getLogger(YandexTranslationApiService.class);

    public YandexTranslationApiService(
            @Value("${translation.api.url.translate}") String translateUrl,
            @Value("${translation.api.url.languages}") String languagesUrl,
            @Value("${translation.api.key}") String key) {
        this.translateUrl = translateUrl;
        this.languagesUrl = languagesUrl;
        this.key = key;
        this.restTemplate = new RestTemplate();
    }

    public String makeTranslateRequest(String word, String sourceLanguage, String targetLanguage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Api-Key " + key);

        TranslationApiRequest requestBody = new TranslationApiRequest(sourceLanguage, targetLanguage, Collections.singletonList(word));
        HttpEntity<TranslationApiRequest> entity = new HttpEntity<>(requestBody, headers);

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
                throw new TranslationApiServiceException("Unexpected response format or empty response");
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
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
                        log.error("Max retry limit reached. API request error: {}", e.getResponseBodyAsString());
                        throw new RateLimitExceededException("Max retry limit reached. API request error: " + e.getResponseBodyAsString(), e);
                    }
                } else {
                    log.error("API request error: {}", e.getResponseBodyAsString());
                    throw new TranslationApiServiceException("API request error: " + e.getResponseBodyAsString(), e);
                }
            } catch (Exception e) {
                log.error("An error occurred while translating text", e);
                throw new TranslationApiServiceException("An error occurred while translating text", e);
            }
        }
        throw new TranslationApiServiceException("Translation failed after " + maxRetries + " attempts.");
    }

    @Override
    public List<Language> makeSupportLanguagesRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Api-Key " + key);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

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
                throw new TranslationApiServiceException("Failed to retrieve supported languages. Unexpected response format.");
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("API request error: {}", e.getResponseBodyAsString());
            throw new TranslationApiServiceException("API request error: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("An error occurred while retrieving supported languages", e);
            throw new TranslationApiServiceException("An error occurred while retrieving supported languages", e);
        }
    }
}