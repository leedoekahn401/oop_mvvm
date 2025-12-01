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
        List<Media> items = new ArrayList<>();
        // In "Analyze Only" mode, we fetch items that need analysis
        // This usually means sentiment is 0.0 AND damageType is UNKNOWN or missing
        Bson filter = Filters.and(
                Filters.eq("topic", topic),
                Filters.or(
                        Filters.eq("sentiment", 0.0),
                        Filters.eq("damageType", "UNKNOWN"),
                        Filters.exists("damageType", false)
                )
        );

        for (Document doc : collection.find(filter).limit(50)) { // Process in batches
            items.add(mapDocumentToMedia(doc));
        }
        return items;
    }

    @Override
    public long countByTopic(String topic) {
        return collection.countDocuments(Filters.eq("topic", topic));
    }

    @Override
    public double getAverageSentiment(String topic) {
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

        // Aggregation: Group by damageType and count
        AggregateIterable<Document> results = collection.aggregate(Arrays.asList(
                Aggregates.match(Filters.and(
                        Filters.eq("topic", topic),
                        Filters.ne("damageType", "UNKNOWN"), // Exclude UNKNOWN from charts
                        Filters.ne("damageType", null)
                )),
                Aggregates.group("$damageType", Accumulators.sum("count", 1))
        ));

        for (Document doc : results) {
            String typeCode = doc.getString("_id");
            if (typeCode == null) continue;

            try {
                String displayName = DamageCategory.valueOf(typeCode).getDisplayName();
                distribution.put(displayName, doc.getInteger("count"));
            } catch (IllegalArgumentException e) {
                // If DB has a value not in Enum (e.g. older data), treat as Other
                distribution.merge("Other", doc.getInteger("count"), Integer::sum);
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

        FindIterable<Document> docs = collection.find(Filters.eq("topic", topic))
                .projection(projection);

        for (Document doc : docs) {
            Date date = doc.getDate("timestamp");
            if (date == null) continue;

            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String type = doc.getString("type");
            if (type == null) type = "unknown";

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

    // --- HELPER METHODS ---

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

    private Media mapDocumentToMedia(Document doc) {
        String topic = doc.getString("topic");
        String content = doc.getString("content");
        String url = doc.getString("url");
        Date timestamp = doc.getDate("timestamp");
        double sentiment = getSafeDouble(doc, "sentiment");

        String dmgStr = doc.getString("damageType");
        DamageCategory damage = DamageCategory.fromString(dmgStr);

        String type = doc.getString("type");

        Media media;
        if ("news".equalsIgnoreCase(type)) {
            String source = doc.getString("source");
            media = new News(topic, content, source, url, timestamp, sentiment);
        } else {
            // Assuming SocialPost has a compatible constructor
            media = new SocialPost(topic, content, url, timestamp, null, sentiment);
        }
        media.setDamageType(damage);
        return media;
    }
}