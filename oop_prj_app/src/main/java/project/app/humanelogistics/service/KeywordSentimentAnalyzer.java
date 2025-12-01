package project.app.humanelogistics.service;

public class KeywordSentimentAnalyzer implements SentimentAnalyzer {

    @Override
    public SentimentType analyze(String text) {
        if (text == null || text.isEmpty()) return SentimentType.NEUTRAL;

        String lower = text.toLowerCase();

        if (lower.contains("damage") || lower.contains("fake") ||
                lower.contains("worry") || lower.contains("disaster") || lower.contains("wind")) {
            return SentimentType.NEGATIVE;
        }
        else if (lower.contains("hope") || lower.contains("safe") ||
                lower.contains("solidarity") || lower.contains("thank") || lower.contains("support")) {
            return SentimentType.POSITIVE;
        }

        return SentimentType.NEUTRAL;
    }

    @Override
    public double analyzeScore(String text) {
        SentimentType type = analyze(text);
        switch (type) {
            case POSITIVE: return 1.0;
            case NEGATIVE: return -1.0;
            default: return 0.0;
        }
    }
}