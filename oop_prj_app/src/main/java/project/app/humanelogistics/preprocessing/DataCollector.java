package project.app.humanelogistics.preprocessing;

import project.app.humanelogistics.model.Media;
import java.util.List;

public interface DataCollector {
    /**
     * Collects data based on a query and a date range.
     * Updated to return List<Media> to support polymorphism (News, SocialPost, etc.)
     */
    List<Media> collect(String query, String startDate, String endDate, int limit);
}