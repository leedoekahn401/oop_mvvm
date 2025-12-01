package project.app.humanelogistics;

import project.app.humanelogistics.db.MediaRepository;
import project.app.humanelogistics.db.MongoMediaRepository;
import project.app.humanelogistics.preprocessing.GeminiDamageClassifier;
import project.app.humanelogistics.preprocessing.GoogleNewsCollector;
import project.app.humanelogistics.preprocessing.SentimentGrade;
import project.app.humanelogistics.service.*;

import java.util.Scanner;

public class DataIngestionApp {

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("   HUMANE LOGISTICS - DATA INGESTION TOOL");
        System.out.println("==========================================");

        // === DEPENDENCY SETUP (Manual DI) ===
        String dbConn = Config.getDbConnectionString();
        MediaRepository repository = new MongoMediaRepository(dbConn, "storm_data", "news");

        // Create services
        SentimentAnalyzer sentimentAnalyzer = new SentimentGrade();
        GeminiDamageClassifier damageClassifier = new GeminiDamageClassifier();
        ContentFetchService contentFetcher = new ContentFetchService();

        ContentEnrichmentService enrichmentService = new ContentEnrichmentService(
                sentimentAnalyzer, damageClassifier, contentFetcher
        );

        DataIngestionService ingestionService = new DataIngestionService(repository);

        AnalysisOrchestrator orchestrator = new AnalysisOrchestrator(
                ingestionService, enrichmentService, repository
        );

        // Register collectors
        orchestrator.registerCollector(new GoogleNewsCollector());

        // === USER INPUT ===
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Search Topic (default: Typhoon Yagi): ");
        String topic = scanner.nextLine().trim();
        if (topic.isEmpty()) topic = "Typhoon Yagi";

        System.out.print("Enter Start Date [M/d/yyyy] (default: 9/4/2024): ");
        String startDate = scanner.nextLine().trim();
        if (startDate.isEmpty()) startDate = "9/4/2024";

        System.out.print("Enter End Date [M/d/yyyy] (default: 11/30/2024): ");
        String endDate = scanner.nextLine().trim();
        if (endDate.isEmpty()) endDate = "11/30/2024";

        System.out.println("\n--- Select Mode ---");
        System.out.println("   [1] Search Only");
        System.out.println("   [2] Search + Analyze");
        System.out.println("   [3] Analyze Existing Data");
        System.out.print("Enter choice: ");

        String choice = scanner.nextLine().trim();

        // === EXECUTION ===
        switch (choice) {
            case "1":
                System.out.println("\n>>> SEARCH ONLY <<<");
                orchestrator.processNewData(topic, startDate, endDate, false);
                break;
            case "2":
                System.out.println("\n>>> FULL ANALYSIS <<<");
                orchestrator.processNewData(topic, startDate, endDate, true);
                break;
            case "3":
                System.out.println("\n>>> ANALYZE EXISTING DATA <<<");
                orchestrator.processExistingData(topic);
                break;
            default:
                System.out.println("Invalid choice. Exiting.");
        }

        System.out.println("\n==========================================");
        System.out.println("   OPERATION COMPLETE");
        System.out.println("==========================================");

        scanner.close();
    }
}