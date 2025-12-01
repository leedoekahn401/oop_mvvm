package project.app.humanelogistics.db;

import com.mongodb.client.*;
import org.bson.Document;
import project.app.humanelogistics.model.DamageCategory;
import project.app.humanelogistics.model.Media;
import project.app.humanelogistics.model.News;
import project.app.humanelogistics.model.SocialPost;
import java.util.ArrayList;
import java.util.List;

public class MongoMediaRepository implements MediaRepository {
    private final MongoCollection<Document> collection;

    public MongoMediaRepository(String connectionString, String dbName, String collName) {
        try {
            MongoClient client = MongoClients.create(connectionString);
            MongoDatabase db = client.getDatabase(dbName);
            this.collection = db.getCollection(collName);
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
            throw new RuntimeException("Failed to connect to MongoDB", e);
        }
    }

    @Override
    public void save(Media item) {
        if(findByContent(item.getContent())) return;

        // Base document with fields common to ALL Media (including URL now)
        Document doc = new Document("topic", item.getTopic())
                .append("content", item.getContent())
                .append("url", item.getUrl())
                .append("timestamp", item.getTimestamp())
                .append("sentiment", item.getSentiment())
                .append("damageType", item.getDamageType().name());

        // Type-specific fields
        if (item instanceof News) {
            News news = (News) item;
            doc.append("source", news.getSource());
            doc.append("type", "news");
        } else if (item instanceof SocialPost) {
            SocialPost post = (SocialPost) item;
            doc.append("comments", post.getComments());
            doc.append("type", "social_post");
        } else {
            doc.append("type", "generic");
        }

        collection.insertOne(doc);
    }

    private boolean findByContent(String content) {
        return collection.find(new Document("content", content)).first() != null;
    }

    @Override
    public void updateSentiment(Media item, Double sentiment) {
        collection.updateOne(
                new Document("content", item.getContent()).append("topic", item.getTopic()),
                new Document("$set", new Document("sentiment", sentiment))
        );
    }

    // NEW METHOD IMPLEMENTATION
    @Override
    public void updateAnalysis(Media item) {
        collection.updateOne(
                new Document("content", item.getContent()).append("topic", item.getTopic()),
                new Document("$set", new Document()
                        .append("sentiment", item.getSentiment())
                        .append("damageType", item.getDamageType().name())
                )
        );
    }

    @Override
    public List<Media> findByTopic(String topic) {
        List<Media> items = new ArrayList<>();
        if (collection == null) return items;

        try {
            FindIterable<Document> docs = collection.find(new Document("topic", topic));
            for (Document doc : docs) {
                String type = doc.getString("type");
                if(type == null) type = "news";

                Double sentiment = 0.0;
                Object sObj = doc.get("sentiment");
                if (sObj instanceof Double) sentiment = (Double) sObj;
                else if (sObj instanceof Integer) sentiment = ((Integer) sObj).doubleValue();
                else if (sObj instanceof String) try { sentiment = Double.parseDouble((String)sObj); } catch(Exception e){}

                // Load URL (common field)
                String url = doc.getString("url");
                if(url == null) url = "";

                // Load Damage Category safely
                String dTypeStr = doc.getString("damageType");
                DamageCategory dType = DamageCategory.fromString(dTypeStr);

                Media mediaItem;
                if ("news".equals(type)) {
                    mediaItem = new News(
                            doc.getString("topic"),
                            doc.getString("content"),
                            doc.getString("source"),
                            url,
                            doc.getDate("timestamp"),
                            sentiment
                    );
                } else {
                    mediaItem = new SocialPost(
                            doc.getString("topic"),
                            doc.getString("content"),
                            url,
                            doc.getDate("timestamp"),
                            doc.getList("comments", String.class),
                            sentiment
                    );
                }

                mediaItem.setDamageType(dType);
                items.add(mediaItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }
}