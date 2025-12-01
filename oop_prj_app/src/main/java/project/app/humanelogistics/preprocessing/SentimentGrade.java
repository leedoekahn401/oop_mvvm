package project.app.humanelogistics.preprocessing;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import project.app.humanelogistics.Config;
import project.app.humanelogistics.service.SentimentAnalyzer;

public class SentimentGrade implements SentimentAnalyzer {

    private final Client client;

    public SentimentGrade() {
        // USE CONFIG CLASS FOR ROBUST KEY LOADING
        String apiKey = Config.getApiKey();

        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Warning: initializing SentimentGrade without valid API key.");
            this.client = null;
        } else {
            // Initialize Client with the builder pattern provided by the SDK
            this.client = Client.builder().apiKey(apiKey).build();
        }
    }

    @Override
    public double analyzeScore(String text) {
        if (text == null || text.trim().isEmpty()) return 0.0;
        if (client == null) {
            System.err.println("Error: Gemini Client is null (Check API Key)");
            return 0.0;
        }

        try {
            String prompt = "Analyze the sentiment of this text regarding a disaster. " +
                    "Rate it on a scale from -1.0 (extremely negative/critical) to 1.0 (extremely positive/hopeful). " +
                    "0.0 is neutral. Respond with ONLY the numeric value (e.g., -0.5, 0.8, 0.0). No words.\n\nPost: " + text;

            GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.0-flash", // Ensure this model is available to your API key
                    prompt,
                    null
            );

            String resultText = response.text();
            if (resultText != null) {
                // Clean up any stray markdown or whitespace
                resultText = resultText.replaceAll("[^\\d.-]", "");
                if (resultText.isEmpty()) return 0.0;
                return Double.parseDouble(resultText.trim());
            }

        } catch (Exception e) {
            System.err.println("Gemini API Failure: " + e.getMessage());
        }
        return 0.0;
    }

    @Override
    public SentimentType analyze(String text) {
        double score = analyzeScore(text);
        if (score > 0.1) return SentimentType.POSITIVE;
        if (score < -0.1) return SentimentType.NEGATIVE;
        return SentimentType.NEUTRAL;
    }

    // === MAIN METHOD FOR TESTING ===
    public static void main(String[] args) {
        System.out.println("--- Testing SentimentGrade Standalone ---");

        // 1. Create Instance
        SentimentGrade analyzer = new SentimentGrade();

        // 2. Define Test Cases
        String[] testCases = {
                "Typhoon Yagi destroyed 500 homes, leaving thousands homeless.",
                "Volunteers are distributing food and water. We will rebuild together!",
                "The weather forecast says it will rain tomorrow."
        };

        // 3. Run Analysis
        for (String text : testCases) {
            System.out.println("Analyzing: " + text);
            double score = analyzer.analyzeScore(text);
            SentimentType type = analyzer.analyze(text);
            System.out.printf("Result: %.2f (%s)%n%n", score, type);
        }
    }
}