package en.pchz.common;

public record TranslationRequest(String sourceCode, String targetCode, String sourceText) {
}