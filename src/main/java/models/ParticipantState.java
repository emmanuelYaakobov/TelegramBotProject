package models;

import java.util.HashSet;
import java.util.Set;

public class ParticipantState {
    private final int totalQuestions;
    private final Set<Integer> answeredQuestionsIds; // שומר אילו שאלות המשתמש כבר ענה
    private boolean reminderSent;

    public ParticipantState(int totalQuestions) {
        this.totalQuestions = totalQuestions;
        this.answeredQuestionsIds = new HashSet<>();
        this.reminderSent = false;
    }

    // הפונקציה נקראת כשהמשתמש עונה על שאלה. מחזירה true אם נקלט בהצלחה, false אם כבר ענה עליה.
    public boolean addAnswer(int questionId) {
        return answeredQuestionsIds.add(questionId);
    }

    public int getAnsweredCount() {
        return answeredQuestionsIds.size();
    }

    public boolean isCompleted() {
        return answeredQuestionsIds.size() == totalQuestions;
    }

    public String getStatusString() {
        if (answeredQuestionsIds.isEmpty()) return "טרם ענה";
        if (isCompleted()) return "השלים";
        return "בתהליך";
    }

    public boolean isReminderSent() { return reminderSent; }
    public void setReminderSent(boolean reminderSent) { this.reminderSent = reminderSent; }
}