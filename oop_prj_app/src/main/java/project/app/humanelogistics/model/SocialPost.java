package project.app.humanelogistics.model;

import java.util.Date;
import java.util.List;

public class SocialPost extends Media {
    private List<String> comments;

    // Updated constructor to accept URL
    public SocialPost(String topic, String content, String url, Date timestamp, List<String> comments, double sentiment) {
        super(topic, content, url, timestamp, sentiment);
        this.comments = comments;
    }

    // Overloaded constructor for backward compatibility (defaults URL to empty)
    public SocialPost(String topic, String content, Date timestamp, List<String> comments) {
        this(topic, content, "", timestamp, comments, 0.0);
    }

    public List<String> getComments() { return comments; }
}