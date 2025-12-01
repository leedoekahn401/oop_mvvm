package project.app.humanelogistics.preprocessing;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import project.app.humanelogistics.Config;
import project.app.humanelogistics.model.DamageCategory;

public class GeminiDamageClassifier implements ContentClassifier {

    private final Client client;

    public GeminiDamageClassifier() {
        String apiKey = Config.getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Warning: initializing GeminiDamageClassifier without valid API key.");
            this.client = null;
        } else {
            this.client = Client.builder().apiKey(apiKey).build();
        }
    }

    @Override
    public DamageCategory classify(String text) {
        if (text == null || text.trim().isEmpty()) return DamageCategory.UNKNOWN;
        if (client == null) return DamageCategory.UNKNOWN;

        try {
            // Truncate extremely long articles to avoid token limits (e.g., first 3000 chars)
            String safeText = text.length() > 3000 ? text.substring(0, 3000) : text;

            String prompt = "Classify this text regarding a disaster into EXACTLY ONE of these categories:\n" +
                    "- AFFECTED_PEOPLE (deaths, injuries, missing, evacuees)\n" +
                    "- ECONOMIC_IMPACT (farms destroyed, factories closed, jobs lost)\n" +
                    "- HOUSING_DAMAGE (roofs blown off, flooded homes, collapsed walls)\n" +
                    "- LOSS_OF_BELONGINGS (vehicles, clothes, electronics lost)\n" +
                    "- INFRASTRUCTURE_DAMAGE (bridges, roads, power lines, internet)\n" +
                    "- OTHER (if it mentions damage but doesn't fit above)\n" +
                    "- UNKNOWN (if it is general news or not about specific damage)\n\n" +
                    "Return ONLY the category name (e.g., HOUSING_DAMAGE). No other text.\n\n" +
                    "Text: " + safeText;

            GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.5-flash",
                    prompt,
                    null
            );

            String resultText = response.text();
            if (resultText != null) {
                return DamageCategory.fromString(resultText.trim());
            }

        } catch (Exception e) {
            System.err.println("Gemini Classification Failure: " + e.getMessage());
        }
        return DamageCategory.UNKNOWN;
    }
}