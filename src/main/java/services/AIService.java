package services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AIService {

    // הטוקן שהתקבל מהמרצה
    private static final String API_KEY = "F96LhZpGkMkI4pKflpD2vRmrkudCvmBEJHA3VMA8ai0uEuQNNLQwYo21TAvQ3Z3g";

    // במידה והמרצה סיפק כתובת Proxy ייעודית של המכללה/אוניברסיטה, יש להחליף את הכתובת כאן
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    public String generateSurvey(String topic) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String prompt = "צור סקר בפורמט JSON בלבד עבור הנושא: " + topic + ". "
                    + "הסקר כולל 3 שאלות, ולכל שאלה 4 תשובות. "
                    + "המבנה חייב להיות בדיוק: {\"topic\":\"" + topic + "\",\"questions\":[{\"id\":1,\"text\":\"...\",\"options\":[\"...\"]}]}";

            String jsonPayload = "{"
                    + "\"model\": \"gpt-3.5-turbo\","
                    + "\"messages\": [{\"role\": \"user\", \"content\": \"" + prompt.replace("\"", "\\\"") + "\"}]"
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // החזרת התשובה המתקבלת מהשרת
                return response.body();
            }
        } catch (Exception e) {
            System.err.println("שגיאה בפנייה ל-API, עובר למצב גיבוי (Mock): " + e.getMessage());
        }

        // גיבוי למקרה של שגיאת תקשורת או מפתח לא פעיל
        return getMockResponse(topic);
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