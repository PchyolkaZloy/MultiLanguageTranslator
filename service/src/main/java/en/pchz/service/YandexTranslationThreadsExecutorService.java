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
        int sentTaskAmount = 0;

        while (sentTaskAmount < words.size()) {
            int doneTaskAmount = (int) futureList.stream().filter(Future::isDone).count();

            if (doneTaskAmount > 0 || futureList.isEmpty()) {
                int additionalTasks = Math.min(
                        translationLimitPerSecond - (sentTaskAmount - doneTaskAmount),
                        words.size() - sentTaskAmount);

                for (int j = 0; j < additionalTasks; j++) {
                    String word = words.get(sentTaskAmount + j);
                    if (word.isEmpty()) continue;

                    log.debug("Submitting additional translation task for word: {}", word);
                    futureList.add(executorService.submit(() -> apiService.makeTranslateRequest(word, sourceCode, targetCode)));
                }
                sentTaskAmount += additionalTasks;
            }

            try {
                log.debug("Waiting for tasks to complete, checking every second...");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("Main thread interrupted during sleep", e);
                throw new TranslationInterruptedException("Main thread interrupted during sleep", e);
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
