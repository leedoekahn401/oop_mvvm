package project.app.humanelogistics.preprocessing;

import project.app.humanelogistics.db.MediaRepository;
import project.app.humanelogistics.db.MongoMediaRepository;
import project.app.humanelogistics.model.Media;
import java.util.List;

public class NewsIngestionTask {

    public static void main(String[] args) {
        System.out.println("--- Starting News Ingestion Task ---");

        // 1. SETUP DB: Point to 'news' collection
        // We use the Interface 'MediaRepository' for the variable type
        // But we instantiate the Concrete Class 'MongoPostRepository'
        // The 3rd argument "news" tells MongoDB to use that specific collection.
        String dbConn = "mongodb+srv://ducanh4012006_db_user:5zEVVC3o7Sjnl2le@cluster0.dwzpibi.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
        MediaRepository newsRepo = new MongoMediaRepository(dbConn, "storm_data", "news");

        // 2. COLLECT
        // We use the GoogleNewsCollector which returns a list of Medias (specifically News objects)
        DataCollector collector = new GoogleNewsCollector();

        String query = "Typhoon Yagi Bão Yagi Vietnam news";
        String startDate = "9/5/2024";
        String endDate = "9/10/2024";

        System.out.println("Collector: Google News");
        System.out.println("Target DB Collection: news");
        System.out.println("Query: " + query);

        // Collect data
        List<Media> articles = collector.collect(query, startDate, endDate, 3);
        System.out.println("Fetched " + articles.size() + " articles.");

        // 3. SAVE
        // Loop through the items and save them.
        // The repository handles the details of writing to MongoDB.
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

        System.out.println("--- Ingestion Complete. Saved " + savedCount + " articles to 'news' collection. ---");
    }
}