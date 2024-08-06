package en.pchz.service;

import en.pchz.common.Language;

import java.util.List;

public interface TranslationApiService {
    String makeTranslateRequest(String word, String sourceLanguage, String targetLanguage);

    List<Language> makeSupportLanguagesRequest();
}
