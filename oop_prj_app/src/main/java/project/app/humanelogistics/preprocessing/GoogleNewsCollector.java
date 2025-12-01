package project.app.humanelogistics.preprocessing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import project.app.humanelogistics.model.Media;
import project.app.humanelogistics.model.News;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GoogleNewsCollector implements DataCollector {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    @Override
    public List<Media> collect(String query, String startDate, String endDate, int pagesToScrape) {
        List<Media> collectedPosts = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);

            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                String dateStr = date.format(formatter);
                System.out.println("Scraping for date: " + dateStr);

                String url = String.format("https://www.google.com/search?q=%s&tbm=nws&tbs=cdr:1,cd_min:%s,cd_max:%s&hl=en",
                        encodedQuery, dateStr, dateStr);

                try {
                    Document doc = Jsoup.connect(url)
                            .userAgent(USER_AGENT)
                            .header("Accept-Language", "en-US,en;q=0.9")
                            .timeout(5000)
                            .get();

                    Date currentDayTimestamp = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    List<Media> dailyPosts = parseDocument(doc, query, currentDayTimestamp);

                    for (Media m : dailyPosts) {
                        if (m instanceof News) {
                            News n = (News) m;
                            System.out.printf("   [FOUND] %s | %s%n", n.getSource(), n.getUrl());
                        }
                    }

                    collectedPosts.addAll(dailyPosts);
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println("Error scraping date " + dateStr + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Collection Error: " + e.getMessage());
        }

        if (collectedPosts.isEmpty()) {
            collectedPosts = generateMockData(query);
        }

        return collectedPosts;
    }

    private List<Media> parseDocument(Document doc, String topic, Date forceDate) {
        List<Media> posts = new ArrayList<>();
        Elements articles = doc.select("div.SoaBEf, a.WlydOe");

        for (Element el : articles) {
            String title = el.select("div[role='heading'], div.n0jPhd").text();

            String rawLink = el.attr("abs:href");
            String cleanLink = cleanGoogleUrl(rawLink);

            String source = el.select("div.MgUUmf, span.NUnG9d").text();

            if(!title.isEmpty() && !cleanLink.isEmpty()) {
                // Updated Constructor: topic, title, source, url, timestamp, sentiment
                posts.add(new News(topic, title, source, cleanLink, forceDate, 0.0));
            }
        }
        return posts;
    }

    private String cleanGoogleUrl(String rawUrl) {
        if (rawUrl == null) return "";
        try {
            if (rawUrl.contains("/url?q=")) {
                String[] parts = rawUrl.split("url\\?q=");
                if (parts.length > 1) {
                    String clean = parts[1].split("&")[0];
                    return URLDecoder.decode(clean, StandardCharsets.UTF_8);
                }
            }
            return rawUrl;
        } catch (Exception e) {
            return rawUrl;
        }
    }

    private List<Media> generateMockData(String topic) {
        List<Media> mocks = new ArrayList<>();
        long now = System.currentTimeMillis();
        // Updated Constructor
        mocks.add(new News(topic, "Typhoon Yagi Impact", "BBC", "https://www.bbc.com/news/world-asia-68000000", new Date(now), 0.0));
        return mocks;
    }
}