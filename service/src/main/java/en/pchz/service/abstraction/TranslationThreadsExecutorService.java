package en.pchz.service.abstraction;

public interface TranslationThreadsExecutorService {
    String translateWordsByThreads(String sourceCode, String targetCode, String text);
}