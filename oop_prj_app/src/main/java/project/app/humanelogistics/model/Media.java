package project.app.humanelogistics.model;

import java.util.Date;

public abstract class Media {
    protected String topic;
    protected String content;
    protected String url; // MOVED HERE
    protected Date timestamp;
    protected double sentiment;
    protected DamageCategory damageType = DamageCategory.UNKNOWN;

    // Updated Constructor
    public Media(String topic, String content, String url, Date timestamp, double sentiment) {
        this.topic = topic;
        this.content = content;
        this.url = url;
        this.timestamp = timestamp;
        this.sentiment = sentiment;
    }

    // Common Getters and Setters
    public String getTopic() { return topic; }
    public String getContent() { return content; }
    public String getUrl() { return url; } // NEW GETTER
    public Date getTimestamp() { return timestamp; }
    public double getSentiment() { return sentiment; }
    public void setSentiment(double sentiment) { this.sentiment = sentiment; }
    public DamageCategory getDamageType() { return damageType; }
    public void setDamageType(DamageCategory damageType) { this.damageType = damageType; }
}