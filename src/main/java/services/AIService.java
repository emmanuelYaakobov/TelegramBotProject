package services;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class AIService {

    private static final String API_TOKEN = "F96LhZpGkMkI4pKflpD2vRmrkudCvmBEJHA3VMA8ai0uEuQNNLQwYo21TAvQ3Z3g";
    private static final String BASE_URL = "[https://shaitest-production-3066.up.railway.app/api-request](https://shaitest-production-3066.up.railway.app/api-request)";

    public String generateSurvey(String topic) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String prompt = "Return ONLY valid raw JSON with no Markdown, no code blocks, no intro text. Topic: " + topic + ". "
                    + "Create 3 questions, 4 options each. Format MUST be strictly: {\"topic\":\"" + topic + "\",\"questions\":[{\"id\":1,\"text\":\"...\",\"options\":[\"...\"]}]}";

            String encodedPrompt = URLEncoder.encode(prompt, StandardCharsets.UTF_8);
            String requestUrl = BASE_URL + "?token=" + API_TOKEN + "&text=" + encodedPrompt;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(requestUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String rawResponse = response.body();
                System.out.println("=== תשובת ה-AI להתקבל מהשרת ===");
                System.out.println(rawResponse);

                // ניקוי וחילוץ תבנית ה-JSON
                return extractJson(rawResponse);
            }
        } catch (Exception e) {
            System.err.println("שגיאה בפנייה לשרת המרצה, עובר למצב גיבוי: " + e.getMessage());
        }

        return getMockResponse(topic);
    }

    private String extractJson(String text) {
        if (text == null || text.isBlank()) return getMockResponse("");

        // הסרת תגיות עיצוב של Markdown
        String cleaned = text.replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();

        // חילוץ המקטע שבין הסוגריים המסולסלים בלבד
        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');

        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return cleaned.substring(firstBrace, lastBrace + 1);
        }

        return cleaned;
    }

    private String getMockResponse(String topic) {
        return "{\n" +
                "  \"topic\": \"" + topic + "\",\n" +
                "  \"questions\": [\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"text\": \"מהו הדבר שהכי מעניין אותך בתחום ה" + topic + "?\",\n" +
                "      \"options\": [\"התיאוריה והלמידה\", \"הפרקטיקה בשטח\", \"ההיסטוריה של זה\", \"אחר\"]\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 2,\n" +
                "      \"text\": \"עד כמה אתה מרגיש שאתה מבין ב" + topic + "?\",\n" +
                "      \"options\": [\"רמה גבוהה מאוד\", \"רמה בינונית\", \"רק התחלתי ללמוד\", \"כלל לא\"]\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 3,\n" +
                "      \"text\": \"האם היית ממליץ לחברים ללמוד על " + topic + "?\",\n" +
                "      \"options\": [\"כן, בהחלט!\", \"אולי, תלוי למי\", \"לא חושב\", \"ממש לא\"]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }
}