package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Survey {
    private final String topic;
    private final List<Question> questions;
    private final Map<Long, ParticipantState> participants;
    private boolean isActive;

    public Survey(String topic, List<Question> questions, Map<Long, User> currentCommunity) {
        this.topic = topic;
        this.questions = questions;
        this.participants = new HashMap<>();
        this.isActive = true;

        for (Long chatId : currentCommunity.keySet()) {
            this.participants.put(chatId, new ParticipantState(questions.size()));
        }
    }

    public void recordVote(int questionIndex, int optionIndex) {
        if (questionIndex >= 0 && questionIndex < questions.size()) {
            questions.get(questionIndex).addVote(optionIndex);
        }
    }

    public String getTopic() { return topic; }
    public List<Question> getQuestions() { return questions; }
    public Map<Long, ParticipantState> getParticipants() { return participants; }
    public int getTotalQuestions() { return questions.size(); }
    public boolean isActive() { return isActive; }
    public void closeSurvey() { this.isActive = false; }

    // מחלקה פנימית לייצוג תוצאת אופציה בודדת לצורך מיון
    public static class OptionResult implements Comparable<OptionResult> {
        private final String optionText;
        private final int voteCount;
        private final double percentage;

        public OptionResult(String optionText, int voteCount, double percentage) {
            this.optionText = optionText;
            this.voteCount = voteCount;
            this.percentage = percentage;
        }

        public String getOptionText() { return optionText; }
        public int getVoteCount() { return voteCount; }
        public double getPercentage() { return percentage; }

        @Override
        public int compareTo(OptionResult o) {
            // מיון בסדר יורד - מהשכיח ביותר למעט ביותר
            return Integer.compare(o.voteCount, this.voteCount);
        }
    }

    // מחלקה פנימית שמייצגת שאלה
    public static class Question {
        private final int id;
        private final String text;
        private final List<String> options;
        private final int[] optionVotes; // ספירת קולות לכל אופציה

        public Question(int id, String text, List<String> options) {
            this.id = id;
            this.text = text;
            this.options = options;
            this.optionVotes = new int[options.size()];
        }

        public void addVote(int optionIndex) {
            if (optionIndex >= 0 && optionIndex < optionVotes.length) {
                optionVotes[optionIndex]++;
            }
        }

        public int getTotalVotes() {
            int total = 0;
            for (int count : optionVotes) total += count;
            return total;
        }

        // מחזירה את תוצאות האופציות כשהן ממוינות לפי שכיחות (מהגבוה לנמוך)
        public List<OptionResult> getSortedResults() {
            List<OptionResult> results = new ArrayList<>();
            int totalVotes = getTotalVotes();

            for (int i = 0; i < options.size(); i++) {
                int count = optionVotes[i];
                double pct = (totalVotes > 0) ? ((double) count / totalVotes) * 100 : 0.0;
                results.add(new OptionResult(options.get(i), count, pct));
            }

            Collections.sort(results); // מיון לפי קריטריון compareTo שהגדרנו
            return results;
        }

        public int getId() { return id; }
        public String getText() { return text; }
        public List<String> getOptions() { return options; }
    }
}