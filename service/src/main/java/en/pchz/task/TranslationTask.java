package en.pchz.task;

import en.pchz.service.TranslationApiService;

import java.util.concurrent.Callable;

public class TranslationTask implements Callable<String> {
    private final TranslationApiService translationApiService;
    private final String word;
    private final String sourceLanguage;
    private final String targetLanguage;


    public TranslationTask(TranslationApiService translationApiService, String word, String sourceLanguage, String targetLanguage) {
        this.translationApiService = translationApiService;
        this.word = word;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
    }

    @Override
    public String call() {
        System.out.println(Thread.currentThread().getName());
        return translationApiService.makeRequest(word, sourceLanguage, targetLanguage);
    }
}
