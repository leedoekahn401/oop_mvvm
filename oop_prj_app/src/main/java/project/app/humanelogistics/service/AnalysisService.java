package project.app.humanelogistics.service;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import project.app.humanelogistics.db.MediaRepository;
import project.app.humanelogistics.model.DamageCategory;
import project.app.humanelogistics.model.Media;
import project.app.humanelogistics.preprocessing.ContentClassifier;
import project.app.humanelogistics.preprocessing.DataCollector;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class AnalysisService {

    private final Map<String, MediaRepository> repoMap = new LinkedHashMap<>();
    private final SentimentAnalyzer sentimentAnalyzer;
    private final ContentClassifier damageClassifier;
    private final List<DataCollector> collectors = new ArrayList<>();

    public AnalysisService(SentimentAnalyzer sentimentAnalyzer, ContentClassifier damageClassifier) {
        this.sentimentAnalyzer = sentimentAnalyzer;
        this.damageClassifier = damageClassifier;
    }

    public void addRepository(String label, MediaRepository repo) {
        this.repoMap.put(label, repo);
    }

    public void registerCollectors(DataCollector... newCollectors) {
        for (DataCollector collector : newCollectors) {
            this.collectors.add(collector);
        }
    }

    public void clearCollectors() {
        this.collectors.clear();
    }

    public void processNewData(String topic, String startDate, String endDate, boolean analyzeImmediately) {
        System.out.println("Starting Cycle for: " + topic + " [" + startDate + " to " + endDate + "]");

        for (DataCollector collector : collectors) {
            List<Media> freshData = collector.collect(topic, startDate, endDate, 1);
            for(Media item : freshData) {
                if (analyzeImmediately) {
                    analyzeItem(item);
                }
                if (!repoMap.isEmpty()) {
                    repoMap.values().iterator().next().save(item);
                }
            }
        }
    }

    // Process existing items in DB
    public void processExistingData(String topic) {
        System.out.println("Scanning database for un-analyzed items: " + topic);
        int count = 0;

        for (MediaRepository repo : repoMap.values()) {
            List<Media> posts = repo.findByTopic(topic);
            System.out.println("Found " + posts.size() + " items in repo. Checking for missing analysis...");

            for (Media item : posts) {
                // Check if needs analysis (Sentiment is 0 OR Damage is Unknown/Null)
                boolean needsAnalysis = (item.getSentiment() == 0.0) ||
                        (item.getDamageType() == null || item.getDamageType() == DamageCategory.UNKNOWN);

                if (needsAnalysis) {
                    System.out.println(" -> Analyzing: " + item.getContent().substring(0, Math.min(item.getContent().length(), 50)) + "...");
                    analyzeItem(item);

                    // Update the DB record
                    repo.updateAnalysis(item);
                    count++;

                    try { Thread.sleep(500); } catch (InterruptedException e) {}
                }
            }
        }
        System.out.println("Batch Analysis Complete. Updated " + count + " items.");
    }

    private void analyzeItem(Media item) {
        String textToAnalyze = item.getContent();

        String url = item.getUrl();
        if (url != null && !url.isEmpty() && url.startsWith("http")) {
            System.out.print("   [FETCHING] Reading article from URL... ");
            String fullBody = fetchUrlContent(url);

            if (!fullBody.isEmpty()) {
                textToAnalyze = fullBody;
                System.out.println("Success (" + fullBody.length() + " chars)");
            } else {
                System.out.println("Failed/Skipped (Using snippet)");
            }
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
                System.out.println("   [RESULT] Score: " + item.getSentiment() + " | Type: " + cat);
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

    public int getTotalPostCount(String topic) {
        int total = 0;
        for (MediaRepository repo : repoMap.values()) {
            total += repo.findByTopic(topic).size();
        }
        return total;
    }

    public double getOverallAverageScore(String topic) {
        double totalScore = 0.0;
        int count = 0;
        for (MediaRepository repo : repoMap.values()) {
            List<Media> posts = repo.findByTopic(topic);
            for (Media post : posts) {
                if (post.getSentiment() != 0.0) {
                    totalScore += post.getSentiment();
                    count++;
                }
            }
        }
        return count == 0 ? 0.0 : totalScore / count;
    }

    public TimeSeriesCollection getSentimentData(String topic, int targetYear) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        for (Map.Entry<String, MediaRepository> entry : repoMap.entrySet()) {
            String seriesName = entry.getKey();
            MediaRepository repo = entry.getValue();
            List<Media> posts = repo.findByTopic(topic);
            Map<LocalDate, Double> dailyTotal = new TreeMap<>();
            Map<LocalDate, Integer> dailyCount = new HashMap<>();
            boolean hasData = false;

            for (Media post : posts) {
                if (post.getTimestamp() == null || post.getSentiment() == 0.0) continue;
                LocalDate localDate = post.getTimestamp().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                if (targetYear != 0 && localDate.getYear() != targetYear) continue;

                dailyTotal.put(localDate, dailyTotal.getOrDefault(localDate, 0.0) + post.getSentiment());
                dailyCount.put(localDate, dailyCount.getOrDefault(localDate, 0) + 1);
                hasData = true;
            }

            TimeSeries series = new TimeSeries(seriesName);
            for (LocalDate date : dailyTotal.keySet()) {
                double avg = dailyTotal.get(date) / dailyCount.get(date);
                series.addOrUpdate(new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear()), avg);
            }
            if (hasData) dataset.addSeries(series);
        }
        return dataset;
    }

    // UPDATED: Filter out UNKNOWN and NULL types
    public DefaultCategoryDataset getDamageData(String topic) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<DamageCategory, Integer> counts = new HashMap<>();

        for (MediaRepository repo : repoMap.values()) {
            List<Media> posts = repo.findByTopic(topic);
            for (Media post : posts) {
                DamageCategory type = post.getDamageType();
                if (type != null && type != DamageCategory.UNKNOWN) {
                    counts.put(type, counts.getOrDefault(type, 0) + 1);
                }
            }
        }

        for (Map.Entry<DamageCategory, Integer> entry : counts.entrySet()) {
            dataset.addValue(entry.getValue(), "Damage Reports", entry.getKey().getDisplayName());
        }

        return dataset;
    }
}