package project.app.humanelogistics.preprocessing;

import project.app.humanelogistics.Config;
import project.app.humanelogistics.db.MediaRepository;
import project.app.humanelogistics.db.MongoMediaRepository;
import project.app.humanelogistics.model.Media;
import java.util.List;

public class NewsIngestionTask {

    public static void main(String[] args) {
        System.out.println("--- Starting News Ingestion Task ---");

        // 1. SETUP DB: Use Config to get the connection string safely
        String dbConn = Config.getDbConnectionString();

        try {
            MediaRepository newsRepo = new MongoMediaRepository(dbConn, "storm_data", "news");

            // 2. COLLECT
            DataCollector collector = new GoogleNewsCollector();
            String query = "Typhoon Yagi Bão Yagi Vietnam news";

            // Note: Dates should ideally be dynamic or args
            String startDate = "9/5/2024";
            String endDate = "9/10/2024";

            System.out.println("Collector: Google News");
            System.out.println("Query: " + query);

            List<Media> articles = collector.collect(query, startDate, endDate, 3);
            System.out.println("Fetched " + articles.size() + " articles.");

            // 3. SAVE
            System.out.println("Saving to MongoDB...");
            int savedCount = 0;
            for (Media article : articles) {
                try {
                    newsRepo.save(article);
                    savedCount++;
                } catch (Exception e) {
                    System.err.println("Error saving article: " + e.getMessage());
                }
            }
            System.out.println("--- Complete. Saved " + savedCount + " articles. ---");

        } catch (Exception e) {
            System.err.println("CRITICAL FAILURE: " + e.getMessage());
            e.printStackTrace();
        }
    }
}