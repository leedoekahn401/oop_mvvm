package project.app.humanelogistics.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import project.app.humanelogistics.db.MediaRepository;
import project.app.humanelogistics.model.DamageCategory;
import project.app.humanelogistics.model.Media;
import project.app.humanelogistics.preprocessing.ContentClassifier;
import project.app.humanelogistics.preprocessing.DataCollector;

import java.time.LocalDate;
import java.util.*;

public class AnalysisService {

    private final Map<String, MediaRepository> repoMap = new LinkedHashMap<>();
    private final SentimentAnalyzer sentimentAnalyzer;
    private final ContentClassifier damageClassifier;

    // List to hold collectors (Google News, etc.)
    private final List<DataCollector> collectors = new ArrayList<>();

    public AnalysisService(SentimentAnalyzer sentimentAnalyzer, ContentClassifier damageClassifier) {
        this.sentimentAnalyzer = sentimentAnalyzer;
        this.damageClassifier = damageClassifier;
    }

    public void addRepository(String label, MediaRepository repo) {
        this.repoMap.put(label, repo);
    }

    // --- COLLECTOR MANAGEMENT (Restored for DataIngestionApp) ---

    public void registerCollectors(DataCollector... newCollectors) {
        for (DataCollector collector : newCollectors) {
            this.collectors.add(collector);
        }
    }

    public void clearCollectors() {
        this.collectors.clear();
    }

    // --- INGESTION LOGIC (Writes to DB) ---

    public void processNewData(String topic, String startDate, String endDate, boolean analyzeImmediately) {
        System.out.println("Starting Cycle for: " + topic + " [" + startDate + " to " + endDate + "]");

        for (DataCollector collector : collectors) {
            List<Media> freshData = collector.collect(topic, startDate, endDate, 1);
            for(Media item : freshData) {
                if (analyzeImmediately) {
                    analyzeItem(item); // Enrichment
                }
                if (!repoMap.isEmpty()) {
                    repoMap.values().iterator().next().save(item);
                }
            }
        }
    }

    public void processExistingData(String topic) {
        System.out.println("Scanning database for un-analyzed items: " + topic);
        int count = 0;

        for (MediaRepository repo : repoMap.values()) {
            List<Media> posts = repo.findByTopic(topic);
            System.out.println("Found " + posts.size() + " items in repo. Checking for missing analysis...");

            for (Media item : posts) {
                boolean needsAnalysis = (item.getSentiment() == 0.0) ||
                        (item.getDamageType() == null || item.getDamageType() == DamageCategory.UNKNOWN);

                if (needsAnalysis) {
                    analyzeItem(item); // Enrichment
                    repo.updateAnalysis(item);
                    count++;
                }
            }
        }
        System.out.println("Batch Analysis Complete. Updated " + count + " items.");
    }

    // --- OPTIMIZED READ LOGIC (For DashboardViewModel) ---

    public long getTotalPostCount(String topic) {
        long total = 0;
        for (MediaRepository repo : repoMap.values()) {
            total += repo.countByTopic(topic);
        }
        return total;
    }

    public double getOverallSentiment(String topic) {
        double totalAvg = 0;
        int count = 0;
        for (MediaRepository repo : repoMap.values()) {
            double avg = repo.getAverageSentiment(topic);
            if (avg != 0.0) {
                totalAvg += avg;
                count++;
            }
        }
        return count == 0 ? 0.0 : totalAvg / count;
    }

    public Map<String, Integer> getAggregatedDamageStats(String topic) {
        Map<String, Integer> totalStats = new HashMap<>();
        for (MediaRepository repo : repoMap.values()) {
            Map<String, Integer> repoStats = repo.getDamageDistribution(topic);
            repoStats.forEach((key, value) ->
                    totalStats.merge(key, value, Integer::sum)
            );
        }
        return totalStats;
    }

    public Map<String, Map<LocalDate, Double>> getSentimentTrends(String topic) {
        Map<String, Map<LocalDate, Double>> allTrends = new HashMap<>();

        for (Map.Entry<String, MediaRepository> entry : repoMap.entrySet()) {
            String sourceName = entry.getKey(); // "News", "Social Posts"
            Map<String, Map<LocalDate, Double>> repoTrends = entry.getValue().getDailySentimentTrends(topic);

            // Flatten the repo results under the source name
            for(Map<LocalDate, Double> dateMap : repoTrends.values()) {
                allTrends.put(sourceName, dateMap);
            }
        }
        return allTrends;
    }

    // --- HELPER METHODS ---

    private void analyzeItem(Media item) {
        String textToAnalyze = item.getContent();

        String url = item.getUrl();
        if (url != null && !url.isEmpty() && url.startsWith("http")) {
            String fullBody = fetchUrlContent(url);
            if (!fullBody.isEmpty()) textToAnalyze = fullBody;
        }

        try {
            double score = sentimentAnalyzer.analyzeScore(textToAnalyze);
            item.setSentiment(score);
        } catch (Exception e) {
            System.err.println("Sentiment Error: " + e.getMessage());
        }

        try {
            if (damageClassifier != null) {
                DamageCategory cat = damageClassifier.classify(textToAnalyze);
                item.setDamageType(cat);
            }
        } catch (Exception e) {
            System.err.println("Classification Error: " + e.getMessage());
        }
    }

    private String fetchUrlContent(String url) {
        if (url == null || url.isEmpty() || !url.startsWith("http")) return "";
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(5000)
                    .get();
            return doc.select("p").text();
        } catch (Exception e) {
            return "";
        }
    }
}