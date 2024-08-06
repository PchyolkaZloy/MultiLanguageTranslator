package en.pchz.service;

import en.pchz.common.TranslationApiRequest;
import en.pchz.common.TranslationApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class YandexTranslationApiService implements TranslationApiService {
    private final String url;
    private final String key;
    private final RestTemplate restTemplate;

    public YandexTranslationApiService(
            @Value("${translation.api.address}") String url,
            @Value("${translation.api.key}") String token) {
        this.url = url;
        this.key = token;
        this.restTemplate = new RestTemplate();
    }

    public String makeRequest(String word, String sourceLanguage, String targetLanguage) {
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
                        url,
                        HttpMethod.POST,
                        entity,
                        TranslationApiResponse.class);

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    TranslationApiResponse responseBody = response.getBody();
                    if (responseBody.translations() != null && !responseBody.translations().isEmpty()) {
                        return responseBody.translations().getFirst().text();
                    }
                }
                throw new RuntimeException("Unexpected response format or empty response");
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    retryCount++;
                    if (retryCount < maxRetries) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException interruptedException) {
                            throw new RuntimeException("Thread interrupted during sleep", interruptedException);
                        }
                    } else {
                        throw new RuntimeException("Max retry limit reached. API request error: " + e.getResponseBodyAsString(), e);
                    }
                } else {
                    throw new RuntimeException("API request error: " + e.getResponseBodyAsString(), e);
                }
            } catch (Exception e) {
                throw new RuntimeException("An error occurred while translating text", e);
            }
        }
        throw new RuntimeException("Translation failed after " + maxRetries + " attempts.");
    }

}
