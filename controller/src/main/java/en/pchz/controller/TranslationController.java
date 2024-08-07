package en.pchz.controller;

import en.pchz.common.*;
import en.pchz.exception.TranslationApiException;
import en.pchz.service.TranslationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/translator")
public class TranslationController {
    private final TranslationService translationService;

    @Autowired
    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @PostMapping("/translate")
    public ResponseEntity<?> translate(
            @RequestBody TranslationRequest translationRequest,
            HttpServletRequest request) {
        String clientIp = Optional.ofNullable(request.getHeader("X-Forwarded-For"))
                .orElseGet(request::getRemoteAddr);

        if (translationRequest.sourceCode() == null ||
                translationRequest.targetCode() == null ||
                translationRequest.text() == null) {

            return ResponseEntity
                    .badRequest()
                    .body(new SystemResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            "Invalid request: sourceCode, targetCode, and text must not be null")
                    );
        }

        try {
            String translatedText = translationService.translate(
                    LocalDateTime.now(),
                    clientIp,
                    translationRequest.sourceCode(),
                    translationRequest.targetCode(),
                    translationRequest.text()
            );

            return ResponseEntity.ok(new TranslationResponse(translatedText));
        } catch (TranslationApiException apiException) {
            return ResponseEntity
                    .status(apiException.getStatusCode())
                    .body(new SystemResponse(apiException.getStatusCode(), apiException.getMessage()));
        }
    }

    @GetMapping("/languages")
    public ResponseEntity<?> languages() {
        try {
            List<Language> languages = translationService.getAllSupportedLanguage();

            return ResponseEntity.ok(new LanguageResponse(languages));
        } catch (TranslationApiException apiException) {
            return ResponseEntity
                    .status(apiException.getStatusCode())
                    .body(new SystemResponse(apiException.getStatusCode(), apiException.getMessage()));
        }
    }
}
