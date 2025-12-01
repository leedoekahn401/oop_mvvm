package project.app.humanelogistics.model;

import java.util.Date;

public class News extends Media {
    private String source; // e.g., "BBC", "CNN"

    // Updated Constructor to match Media's new signature
    public News(String topic, String title, String source, String url, Date timestamp, double sentiment) {
        // Pass arguments to parent: topic, content, url, timestamp, sentiment
        super(topic, title, url, timestamp, sentiment);
        this.source = source;
    }

    public String getSource() { return source; }
}