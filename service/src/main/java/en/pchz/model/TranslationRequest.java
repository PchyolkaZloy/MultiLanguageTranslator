package en.pchz.model;

import java.util.List;

public record TranslationRequest(
        String sourceLanguageCode,
        String targetLanguageCode,
        List<String> texts) {
}
