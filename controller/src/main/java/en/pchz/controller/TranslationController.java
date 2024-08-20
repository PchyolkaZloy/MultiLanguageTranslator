package en.pchz.controller;

import en.pchz.common.LanguageCode;
import en.pchz.exception.TranslationApiException;
import en.pchz.service.abstraction.TranslationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class TranslationController {
    private final TranslationService translationService;

    @Autowired
    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @RequestMapping("/")
    public String mainPage(
            @RequestParam(defaultValue = "ru") String sourceCode,
            @RequestParam(defaultValue = "en") String targetCode,
            Model model
    ) {
        model.addAttribute("languages", LanguageCode.values());
        model.addAttribute("sourceCode", sourceCode);
        model.addAttribute("targetCode", targetCode);
        model.addAttribute("sourceText", "");
        model.addAttribute("translatedText", "");

        return "main";
    }

    @PostMapping("/translate")
    public String translate(
            @RequestParam String sourceCode,
            @RequestParam String targetCode,
            @RequestParam String sourceText,
            Model model,
            HttpServletRequest request
    ) {
        String clientIp = Optional.ofNullable(request.getHeader("X-Forwarded-For"))
                .orElseGet(request::getRemoteAddr);

        if (sourceCode == null || targetCode == null || sourceText == null) {
            return "redirect:error";
        }

        try {
            String translatedText = translationService.translate(
                    LocalDateTime.now(),
                    clientIp,
                    sourceCode,
                    targetCode,
                    sourceText
            );

            model.addAttribute("languages", LanguageCode.values());
            model.addAttribute("sourceCode", sourceCode);
            model.addAttribute("targetCode", targetCode);
            model.addAttribute("sourceText", sourceText);
            model.addAttribute("translatedText", translatedText);

            return "main";
        } catch (TranslationApiException apiException) {
            return "redirect:error";
        }
    }
}
