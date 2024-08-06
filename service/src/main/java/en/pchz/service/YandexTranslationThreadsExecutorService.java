package en.pchz.service;

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
        List<String> words = Arrays.asList(text.split(" "));
        List<Future<String>> futureList = new ArrayList<>();

        int totalWords = words.size();
        for (int i = 0; i < totalWords; i += translationLimitPerSecond) {
            int end = Math.min(i + translationLimitPerSecond, totalWords);

            for (int j = i; j < end; j++) {
                String word = words.get(j);
                if (word.isEmpty()) continue;

                futureList.add(executorService.submit(() -> apiService.makeRequest(word, sourceCode, targetCode)));
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread interrupted during sleep", e);
            }
        }

        StringBuilder builder = new StringBuilder();
        for (Future<String> future : futureList) {
            try {
                builder.append(future.get()).append(" ");
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to translate word", e);
            }
        }

        return builder.toString().trim();
    }
}
