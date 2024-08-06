package en.pchz.controller;

import en.pchz.common.TranslationRequest;
import en.pchz.common.TranslationResponse;
import en.pchz.service.TranslationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

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

        return new TranslationResponse(
                200,
                translationService.translate(
                        LocalDateTime.now(),
                        request.getRemoteAddr(),
                        translationRequest.sourceCode(),
                        translationRequest.targetCode(),
                        translationRequest.text()
                )
        );
    }
}
