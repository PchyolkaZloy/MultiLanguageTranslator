package en.pchz.controller;

import en.pchz.common.LanguageResponse;
import en.pchz.common.TranslationRequest;
import en.pchz.common.TranslationResponse;
import en.pchz.service.TranslationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    public TranslationResponse translate(
            @RequestBody TranslationRequest translationRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        response.setStatus(HttpStatus.OK.value());
        String clientIp = Optional.ofNullable(request.getHeader("X-Forwarded-For"))
                .orElseGet(request::getRemoteAddr);


        return new TranslationResponse(
                200,
                translationService.translate(
                        LocalDateTime.now(),
                        clientIp,
                        translationRequest.sourceCode(),
                        translationRequest.targetCode(),
                        translationRequest.text()
                )
        );
    }

    @GetMapping("/languages")
    public LanguageResponse languages(HttpServletResponse response) {
        return new LanguageResponse(translationService.getAllSupportedLanguage());
    }
}
