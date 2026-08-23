package manager;

import bot.SurveyBot;
import gui.DashboardFrame;
import gui.SurveyResultsDialog;
import models.ParticipantState;
import models.Survey;
import models.User;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class SystemManager {
    private final Map<Long, User> community = new ConcurrentHashMap<>();
    private Survey activeSurvey = null;
    private DashboardFrame dashboardFrame;
    private SurveyBot bot;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private ScheduledFuture<?> reminderFuture;
    private ScheduledFuture<?> autoCloseFuture;
    private ScheduledFuture<?> countdownFuture;

    private boolean isDelayed = false;
    private int delaySecondsRemaining = 0;
    private int activeSecondsRemaining = 0;
    private String pendingTopic = "";
    private List<Survey.Question> pendingQuestions = null;

    public void setDashboardFrame(DashboardFrame dashboardFrame) {
        this.dashboardFrame = dashboardFrame;
    }

    public void setBot(SurveyBot bot) {
        this.bot = bot;
    }

    public void registerNewUser(long chatId, String firstName, String lastName, String username) {
        if (!community.containsKey(chatId)) {
            User newUser = new User(chatId, firstName, lastName, username);
            community.put(chatId, newUser);

            if (dashboardFrame != null) {
                dashboardFrame.refreshCommunityTable();
            }

            int communitySize = community.size();
            String broadcastMsg = "🎉 משתמש חדש הצטרף לקהילה: " + newUser.getFullName() + "\n"
                    + "גודל הקהילה הנוכחי: " + communitySize + " חברים.";

            for (Long existingChatId : community.keySet()) {
                if (existingChatId != chatId && bot != null) {
                    bot.sendMessage(existingChatId, broadcastMsg);
                }
            }
        }
    }

    public boolean createCustomSurvey(String topic, List<Survey.Question> questions, int delayMinutes) {
        if ((activeSurvey != null && activeSurvey.isActive()) || isDelayed) {
            return false;
        }

        if (community.size() < 3) {
            return false;
        }

        if (delayMinutes > 0) {
            this.isDelayed = true;
            this.delaySecondsRemaining = delayMinutes * 60;
            this.pendingTopic = topic;
            this.pendingQuestions = questions;

            if (dashboardFrame != null) {
                dashboardFrame.refreshActiveSurveyTable();
            }

            startDelayCountdown();
        } else {
            startSurveyNow(topic, questions);
        }
        return true;
    }

    private void startDelayCountdown() {
        stopCountdownFuture();
        countdownFuture = scheduler.scheduleAtFixedRate(() -> {
            delaySecondsRemaining--;
            if (dashboardFrame != null) {
                dashboardFrame.refreshActiveSurveyTable();
            }

            if (delaySecondsRemaining <= 0) {
                stopCountdownFuture();
                isDelayed = false;
                startSurveyNow(pendingTopic, pendingQuestions);

                if (dashboardFrame != null) {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(dashboardFrame,
                                    "⏰ הספירה לאחור הסתיימה! הסקר נשלח כעת לכל חברי הקהילה.",
                                    "סקר נשלח",
                                    JOptionPane.INFORMATION_MESSAGE)
                    );
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void startSurveyNow(String topic, List<Survey.Question> questions) {
        this.activeSurvey = new Survey(topic, questions, community);
        this.activeSecondsRemaining = 300;

        for (Long chatId : activeSurvey.getParticipants().keySet()) {
            if (bot != null) {
                bot.sendMessage(chatId, "📋 סקר חדש בנושא \"" + topic + "\" יוצא לדרך!");
                Survey.Question firstQ = questions.get(0);
                bot.sendQuestionAsPoll(chatId, firstQ.getText(), firstQ.getOptions());
            }
        }

        if (dashboardFrame != null) {
            dashboardFrame.refreshActiveSurveyTable();
        }

        stopCountdownFuture();
        countdownFuture = scheduler.scheduleAtFixedRate(() -> {
            activeSecondsRemaining--;
            if (dashboardFrame != null) {
                dashboardFrame.refreshActiveSurveyTable();
            }
        }, 1, 1, TimeUnit.SECONDS);

        reminderFuture = scheduler.schedule(this::sendRemindersIfNeeded, 3, TimeUnit.MINUTES);
        autoCloseFuture = scheduler.schedule(this::closeActiveSurvey, 5, TimeUnit.MINUTES);
    }

    private void sendRemindersIfNeeded() {
        if (activeSurvey != null && activeSurvey.isActive()) {
            for (Map.Entry<Long, ParticipantState> entry : activeSurvey.getParticipants().entrySet()) {
                long chatId = entry.getKey();
                ParticipantState state = entry.getValue();

                if (!state.isCompleted() && !state.isReminderSent()) {
                    state.setReminderSent(true);
                    if (bot != null) {
                        bot.sendMessage(chatId, "⏰ תזכורת: נותרו 2 דקות בלבד להשלמת הסקר בנושא \"" + activeSurvey.getTopic() + "\".");
                    }
                }
            }
        }
    }

    public void handlePollAnswer(long chatId, int selectedOptionIndex) {
        if (activeSurvey != null && activeSurvey.isActive()) {
            ParticipantState state = activeSurvey.getParticipants().get(chatId);

            if (state != null) {
                int currentQuestionIndex = state.getAnsweredCount();

                if (state.addAnswer(currentQuestionIndex)) {
                    activeSurvey.recordVote(currentQuestionIndex, selectedOptionIndex);

                    int nextQuestionIndex = state.getAnsweredCount();

                    if (nextQuestionIndex < activeSurvey.getTotalQuestions()) {
                        Survey.Question nextQ = activeSurvey.getQuestions().get(nextQuestionIndex);
                        if (bot != null) {
                            bot.sendQuestionAsPoll(chatId, nextQ.getText(), nextQ.getOptions());
                        }
                    } else {
                        if (bot != null) {
                            bot.sendMessage(chatId, "תודה רבה! ענית על כל השאלות בסקר בהצלחה. 🎉");
                        }
                    }

                    if (dashboardFrame != null) {
                        dashboardFrame.refreshActiveSurveyTable();
                    }

                    checkIfAllCompleted();
                }
            }
        }
    }

    private void checkIfAllCompleted() {
        if (activeSurvey == null) return;
        boolean allDone = activeSurvey.getParticipants().values().stream()
                .allMatch(ParticipantState::isCompleted);
        if (allDone) {
            closeActiveSurvey();
        }
    }

    public void closeActiveSurvey() {
        cancelScheduledTimers();

        if (activeSurvey != null && activeSurvey.isActive()) {
            activeSurvey.closeSurvey();

            Survey completedSurvey = activeSurvey;
            activeSurvey = null;

            if (dashboardFrame != null) {
                dashboardFrame.refreshActiveSurveyTable();

                SwingUtilities.invokeLater(() -> {
                    SurveyResultsDialog resultsDialog = new SurveyResultsDialog(dashboardFrame, completedSurvey);
                    resultsDialog.setVisible(true);
                });
            }
        }
    }

    private void cancelScheduledTimers() {
        if (reminderFuture != null && !reminderFuture.isDone()) {
            reminderFuture.cancel(true);
        }
        if (autoCloseFuture != null && !autoCloseFuture.isDone()) {
            autoCloseFuture.cancel(true);
        }
        stopCountdownFuture();
    }

    private void stopCountdownFuture() {
        if (countdownFuture != null && !countdownFuture.isDone()) {
            countdownFuture.cancel(true);
        }
    }

    public String getFormattedTimerString() {
        if (isDelayed) {
            int mins = Math.max(0, delaySecondsRemaining / 60);
            int secs = Math.max(0, delaySecondsRemaining % 60);
            return String.format("⏳ הסקר מושהה - נשלח בעוד: %02d:%02d", mins, secs);
        } else if (activeSurvey != null && activeSurvey.isActive()) {
            int mins = Math.max(0, activeSecondsRemaining / 60);
            int secs = Math.max(0, activeSecondsRemaining % 60);
            return String.format("⏱️ סקר פעיל: \"%s\" | זמן נותר: %02d:%02d", activeSurvey.getTopic(), mins, secs);
        }
        return "אין סקר פעיל כרגע";
    }

    public boolean isDelayed() { return isDelayed; }
    public Map<Long, User> getCommunity() { return community; }
    public Survey getActiveSurvey() { return activeSurvey; }
}