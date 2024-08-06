package en.pchz.service;

import en.pchz.exception.TranslationInterruptedException;
import en.pchz.exception.TranslationThreadsExecutorServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class YandexTranslationThreadsExecutorService implements TranslationThreadsExecutorService {
    private final TranslationApiService apiService;
    private final ExecutorService executorService;
    private final Integer translationLimitPerSecond;
    private static final Logger log = LoggerFactory.getLogger(YandexTranslationThreadsExecutorService.class);


    @Autowired
    public YandexTranslationThreadsExecutorService(
            TranslationApiService apiService,
            @Value("${translation.thread.limit}") Integer maxThreadsLimit,
            @Value("${translation.translation.limit}") Integer translationLimitPerSecond) {
        this.apiService = apiService;
        this.translationLimitPerSecond = translationLimitPerSecond;
        this.executorService = Executors.newFixedThreadPool(maxThreadsLimit);
    }

    @Override
    public String translateWordsByThreads(String sourceCode, String targetCode, String text) {
        log.info("Starting translation text: from {} to {}", sourceCode, targetCode);
        List<String> words = Arrays.asList(text.split(" "));
        List<Future<String>> futureList = new ArrayList<>();

        int totalWords = words.size();
        for (int i = 0; i < totalWords; i += translationLimitPerSecond) {
            int end = Math.min(i + translationLimitPerSecond, totalWords);

            for (int j = i; j < end; j++) {
                String word = words.get(j);
                if (word.isEmpty()) continue;

                log.debug("Submitting translation task for word: {}", word);
                futureList.add(executorService.submit(() -> apiService.makeTranslateRequest(word, sourceCode, targetCode)));
            }

            try {
                log.debug("Throttling requests by sleeping for 1 second");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("Thread interrupted during sleep", e);
                throw new TranslationInterruptedException("Thread interrupted during sleep", e);
            }
        }

        StringBuilder builder = new StringBuilder();
        for (Future<String> future : futureList) {
            try {
                builder.append(future.get()).append(" ");
            } catch (InterruptedException e) {
                log.error("Translation task interrupted", e);
                throw new TranslationInterruptedException("Translation task interrupted", e);
            } catch (ExecutionException e) {
                log.error("Failed to execute translation task", e);
                throw new TranslationThreadsExecutorServiceException("Failed to translate word", e);
            }
        }
        log.info("Completed translation.");

        return builder.toString().trim();
    }
}
