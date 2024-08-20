package en.pchz.service.implementation;

import en.pchz.common.Language;
import en.pchz.repository.TranslationHistoryRepository;
import en.pchz.service.abstraction.TranslationApiService;
import en.pchz.service.abstraction.TranslationService;
import en.pchz.service.abstraction.TranslationThreadsExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class YandexTranslationService implements TranslationService {
    private final TranslationThreadsExecutorService threadsExecutorService;
    private final TranslationApiService apiService;
    private final TranslationHistoryRepository repository;


    @Autowired
    public YandexTranslationService(
            TranslationHistoryRepository repository,
            TranslationApiService apiService,
            TranslationThreadsExecutorService threadsExecutorService) {
        this.threadsExecutorService = threadsExecutorService;
        this.apiService = apiService;
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
    public List<Language> getAllSupportedLanguage() {
        return apiService.makeSupportLanguagesRequest();
    }
}
