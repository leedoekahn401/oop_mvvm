package project.app.humanelogistics.db;

import project.app.humanelogistics.model.Media;
import java.util.List;

public interface MediaRepository {
    void save(Media item);

    // Existing method, specific to sentiment
    void updateSentiment(Media item, Double sentiment);

    // NEW METHOD: Updates both Sentiment and Damage Type
    void updateAnalysis(Media item);

    List<Media> findByTopic(String topic);
}