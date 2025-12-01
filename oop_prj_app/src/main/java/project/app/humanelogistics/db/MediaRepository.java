package project.app.humanelogistics.db;

import project.app.humanelogistics.model.Media;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MediaRepository {
    void save(Media item);
    void updateAnalysis(Media item);
    List<Media> findByTopic(String topic);

    // --- NEW OPTIMIZED METHODS ---
    long countByTopic(String topic);

    double getAverageSentiment(String topic);

    // Returns Map<DamageCategoryName, Count>
    Map<String, Integer> getDamageDistribution(String topic);

    // Returns Map<SourceType, Map<Date, AverageScore>>
    // e.g. "news" -> { 2024-09-01: 0.5, 2024-09-02: -0.2 }
    Map<String, Map<LocalDate, Double>> getDailySentimentTrends(String topic);
}