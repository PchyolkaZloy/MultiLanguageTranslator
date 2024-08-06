package en.pchz.service;

import java.time.LocalDateTime;
import java.util.List;

public interface TranslationService {
    String translate(LocalDateTime requestTime, String clientIpAddress, String sourceCode, String targetCode, String text);

    List<String> getAllSupportedLanguage();
}
