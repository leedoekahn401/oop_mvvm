package project.app.humanelogistics;

import io.github.cdimascio.dotenv.Dotenv;

public class Config {

    // SECURITY NOTICE:
    // 1. Rename .env.example to .env
    // 2. Add your secrets there.
    // 3. DO NOT commit .env to GitHub.

    // Fallback constants (Should be replaced by Environment Variables in production)
    private static final String DEFAULT_DB_CONN = "mongodb+srv://<USER>:<PASSWORD>@<CLUSTER>.mongodb.net/?retryWrites=true&w=majority";

    private static final Dotenv dotenv;

    static {
        Dotenv temp = null;
        try {
            temp = Dotenv.configure().ignoreIfMissing().load();
        } catch (Exception e) {
            System.err.println("Env file not found, using system env or defaults.");
        }
        dotenv = temp;
    }

    public static String getApiKey() {
        String key = (dotenv != null) ? dotenv.get("GOOGLE_API_KEY") : System.getenv("GOOGLE_API_KEY");
        if (key == null || key.isEmpty()) {
            key = (dotenv != null) ? dotenv.get("GEMINI_API_KEY") : System.getenv("GEMINI_API_KEY");
        }

        if (key == null || key.isEmpty()) {
            System.err.println("CRITICAL: No API Key found. AI features will fail.");
            return "";
        }
        return key;
    }

    public static String getDbConnectionString() {
        String conn = (dotenv != null) ? dotenv.get("DB_CONNECTION_STRING") : System.getenv("DB_CONNECTION_STRING");
        if (conn == null || conn.isEmpty()) {
            // Check if user is trying to use the insecure default
            if (DEFAULT_DB_CONN.contains("<USER>")) {
                System.err.println("ERROR: You must set DB_CONNECTION_STRING in .env or update Config.java");
            }
            return DEFAULT_DB_CONN;
        }
        return conn;
    }
}