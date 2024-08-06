package en.pchz.service;

import en.pchz.repository.TranslationHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class YandexTranslationService implements TranslationService {
    private final TranslationThreadsExecutorService threadsExecutorService;
    private final TranslationHistoryRepository repository;


    @Autowired
    public YandexTranslationService(
            TranslationHistoryRepository repository,
            TranslationThreadsExecutorService threadsExecutorService) {
        this.threadsExecutorService = threadsExecutorService;
        this.repository = repository;
    }

    public String translate(
            LocalDateTime requestTime,
            String clientIpAddress,
            String sourceCode,
            String targetCode,
            String text
    ) {
        String translatedText = threadsExecutorService.translateWordsByThreads(sourceCode, targetCode, text);
        repository.save(requestTime, clientIpAddress, text, translatedText);

        return translatedText;
    }


    @Override
    public List<String> getAllSupportedLanguage() {
        return null;
    }
}
