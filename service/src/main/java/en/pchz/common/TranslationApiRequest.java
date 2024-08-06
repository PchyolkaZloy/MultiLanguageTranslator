package en.pchz.common;

import java.util.List;

public record TranslationApiRequest(
        String sourceLanguageCode,
        String targetLanguageCode,
        List<String> texts) {
}