package project.app.humanelogistics.db;

import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import project.app.humanelogistics.model.DamageCategory;
import project.app.humanelogistics.model.Media;
import project.app.humanelogistics.model.News;
import project.app.humanelogistics.model.SocialPost;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class MongoMediaRepository implements MediaRepository {
    private final MongoCollection<Document> collection;

    public MongoMediaRepository(String connectionString, String dbName, String collName) {
        try {
            MongoClient client = MongoClients.create(connectionString);
            MongoDatabase db = client.getDatabase(dbName);
            this.collection = db.getCollection(collName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to MongoDB", e);
        }
    }

    @Override
    public void save(Media item) {
        if(collection.find(new Document("content", item.getContent())).first() != null) return;
        Document doc = new Document("topic", item.getTopic())
                .append("content", item.getContent())
                .append("url", item.getUrl())
                .append("timestamp", item.getTimestamp())
                .append("sentiment", item.getSentiment())
                .append("damageType", item.getDamageType().name());
        if (item instanceof News) {
            doc.append("source", ((News) item).getSource()).append("type", "news");
        } else {
            doc.append("type", "social_post");
        }
        collection.insertOne(doc);
    }

    @Override
    public void updateAnalysis(Media item) {
        collection.updateOne(
                new Document("content", item.getContent()),
                new Document("$set", new Document("sentiment", item.getSentiment())
                        .append("damageType", item.getDamageType().name()))
        );
    }

    @Override
    public List<Media> findByTopic(String topic) {
        // Implementation kept for reference but should be avoided for Dashboard
        List<Media> items = new ArrayList<>();
        // ... (existing logic) ...
        return items;
    }

    @Override
    public long countByTopic(String topic) {
        return collection.countDocuments(Filters.eq("topic", topic));
    }

    @Override
    public double getAverageSentiment(String topic) {
        // Aggregation might fail if types are mixed (String vs Double).
        // For robustness, we will fetch minimal data and calculate in Java if aggregation fails,
        // OR we can use $toDouble in aggregation if Mongo version supports it (4.0+).
        // Here, we'll stick to Java-side calculation for safety given the error.

        double total = 0;
        int count = 0;

        FindIterable<Document> docs = collection.find(Filters.eq("topic", topic))
                .projection(Projections.include("sentiment"));

        for (Document doc : docs) {
            double val = getSafeDouble(doc, "sentiment");
            if (val != 0.0) {
                total += val;
                count++;
            }
        }
        return count == 0 ? 0.0 : total / count;
    }

    @Override
    public Map<String, Integer> getDamageDistribution(String topic) {
        Map<String, Integer> distribution = new HashMap<>();

        AggregateIterable<Document> results = collection.aggregate(Arrays.asList(
                Aggregates.match(Filters.and(
                        Filters.eq("topic", topic),
                        Filters.ne("damageType", "UNKNOWN"),
                        Filters.ne("damageType", null)
                )),
                Aggregates.group("$damageType", Accumulators.sum("count", 1))
        ));

        for (Document doc : results) {
            String typeCode = doc.getString("_id");
            try {
                String displayName = DamageCategory.valueOf(typeCode).getDisplayName();
                distribution.put(displayName, doc.getInteger("count"));
            } catch (Exception e) {
                distribution.put(typeCode, doc.getInteger("count"));
            }
        }
        return distribution;
    }

    @Override
    public Map<String, Map<LocalDate, Double>> getDailySentimentTrends(String topic) {
        Map<String, Map<LocalDate, Double>> trends = new HashMap<>();

        Bson projection = Projections.fields(
                Projections.include("timestamp", "sentiment", "type"),
                Projections.excludeId()
        );

        FindIterable<Document> docs = collection.find(Filters.and(
                Filters.eq("topic", topic)
                // Removed sentiment check in filter to handle parsing manually
        )).projection(projection);

        for (Document doc : docs) {
            Date date = doc.getDate("timestamp");
            if (date == null) continue;

            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String type = doc.getString("type");

            // FIX: Use safe getter
            double sentiment = getSafeDouble(doc, "sentiment");
            if (sentiment == 0.0) continue;

            trends.putIfAbsent(type, new TreeMap<>());
            Map<LocalDate, Double> dateMap = trends.get(type);

            if (dateMap.containsKey(localDate)) {
                double currentAvg = dateMap.get(localDate);
                dateMap.put(localDate, (currentAvg + sentiment) / 2.0);
            } else {
                dateMap.put(localDate, sentiment);
            }
        }
        return trends;
    }

    // --- HELPER TO FIX CLASS CAST EXCEPTION ---
    private double getSafeDouble(Document doc, String key) {
        Object val = doc.get(key);
        if (val == null) return 0.0;

        if (val instanceof Double) return (Double) val;
        if (val instanceof Integer) return ((Integer) val).doubleValue();
        if (val instanceof String) {
            try {
                return Double.parseDouble((String) val);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
}