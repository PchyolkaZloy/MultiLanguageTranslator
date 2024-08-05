package en.pchz.service;

import en.pchz.repository.TranslationHistoryRepository;
import en.pchz.task.TranslationTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class TranslationService {
    private final TranslationApiService apiService;
    private final TranslationHistoryRepository repository;

    @Autowired
    public TranslationService(TranslationApiService apiService,  TranslationHistoryRepository repository) {
        this.apiService = apiService;
        this.repository = repository;
    }

    public String translateWords(List<String> words, String sourceLanguage, String targetLanguage) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Future<String>> futureList = new ArrayList<>();

        for (String word : words) {
            TranslationTask task = new TranslationTask(apiService, word, sourceLanguage, targetLanguage);
            Future<String> future = executorService.submit(task);
            futureList.add(future);
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

        executorService.shutdown();
        return builder.toString().trim();
    }
}
