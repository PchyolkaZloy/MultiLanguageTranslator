package en.pchz.service;

import en.pchz.model.TranslationRequest;
import en.pchz.model.TranslationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
public class TranslationApiService {
    private final String url;
    private final String key;
    private final RestTemplate restTemplate;

    public TranslationApiService(
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

        TranslationRequest requestBody = new TranslationRequest(sourceLanguage, targetLanguage, Collections.singletonList(word));
        HttpEntity<TranslationRequest> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<TranslationResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, TranslationResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                TranslationResponse responseBody = response.getBody();
                if (responseBody.translations() != null && !responseBody.translations().isEmpty()) {
                    return responseBody.translations().getFirst().text();
                }
            }
            throw new RuntimeException("Unexpected response format or empty response");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new RuntimeException("API request error: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while translating text", e);
        }
    }
}
