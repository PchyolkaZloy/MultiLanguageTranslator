package en.pchz.service;

public interface TranslationThreadsExecutorService {
    String translateWordsByThreads(String sourceCode, String targetCode, String text);
}