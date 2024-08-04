package en.pchz.repository;

import java.time.LocalDateTime;


public interface TranslationHistoryRepository {
    void save(LocalDateTime dateTime, String ipAddress, String inputText, String translatedText);
}
