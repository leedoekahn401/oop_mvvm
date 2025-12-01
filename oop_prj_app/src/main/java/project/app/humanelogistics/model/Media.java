package project.app.humanelogistics.model;

import java.util.Date;

public abstract class Media {
    protected String topic;
    protected String content;
    protected String url;
    protected Date timestamp;
    protected double sentiment;
    protected DamageCategory damageType = DamageCategory.UNKNOWN;

    public Media(String topic, String content, String url, Date timestamp, double sentiment) {
        this.topic = topic;
        this.content = content;
        this.url = url;
        this.timestamp = timestamp;
        this.sentiment = sentiment;
    }

    // BEHAVIOR: Domain logic lives in the model!
    public boolean needsAnalysis() {
        return sentiment == 0.0 ||
                damageType == null ||
                damageType == DamageCategory.UNKNOWN;
    }

    public boolean hasContent() {
        return content != null && !content.trim().isEmpty();
    }

    public boolean hasValidUrl() {
        return url != null && url.startsWith("http");
    }

    // Getters and Setters
    public String getTopic() { return topic; }
    public String getContent() { return content; }
    public String getUrl() { return url; }
    public Date getTimestamp() { return timestamp; }
    public double getSentiment() { return sentiment; }
    public void setSentiment(double sentiment) { this.sentiment = sentiment; }
    public DamageCategory getDamageType() { return damageType; }
    public void setDamageType(DamageCategory damageType) { this.damageType = damageType; }
}