package project.app.humanelogistics;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {

    // Fallback constants (Use .env in production!)
    private static final String DEFAULT_DB_CONN = "mongodb+srv://ducanh4012006_db_user:5zEVVC3o7Sjnl2le@cluster0.dwzpibi.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";

    private static final Dotenv dotenv;

    static {
        // Try to load .env, but don't crash if missing
        Dotenv temp = null;
        try {
            temp = Dotenv.configure().ignoreIfMissing().load();
        } catch (Exception e) {
            System.err.println("Env file not found, using system env or defaults.");
        }
        dotenv = temp;
    }

    public static String getApiKey() {
        // Try getting from .env or System Env
        String key = (dotenv != null) ? dotenv.get("GOOGLE_API_KEY") : System.getenv("GOOGLE_API_KEY");

        // Handle the case where user used GEMINI_API_KEY instead
        if (key == null || key.isEmpty()) {
            key = (dotenv != null) ? dotenv.get("GEMINI_API_KEY") : System.getenv("GEMINI_API_KEY");
        }

        if (key == null || key.isEmpty()) {
            System.err.println("CRITICAL: No API Key found in .env or System Variables.");
            return ""; // Return empty to prevent null pointer, but API calls will fail
        }
        return key;
    }

    public static String getDbConnectionString() {
        String conn = (dotenv != null) ? dotenv.get("DB_CONNECTION_STRING") : System.getenv("DB_CONNECTION_STRING");
        if (conn == null || conn.isEmpty()) {
            return DEFAULT_DB_CONN;
        }
        return conn;
    }
}