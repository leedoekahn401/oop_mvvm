package project.app.humanelogistics.service;

public interface SentimentAnalyzer {
    enum SentimentType { POSITIVE, NEGATIVE, NEUTRAL }

    SentimentType analyze(String text);
    double analyzeScore(String text);
}