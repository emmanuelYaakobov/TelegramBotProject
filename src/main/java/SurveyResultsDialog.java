package gui;

import models.Survey;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.util.List;

public class SurveyResultsDialog extends JDialog {

    public SurveyResultsDialog(Frame parent, Survey survey) {
        super(parent, "תוצאות הסקר - " + survey.getTopic(), true);

        setSize(650, 550);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        initUI(survey);
    }

    private void initUI(Survey survey) {
        // פאנל כותרת וסיכום כללי
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        headerPanel.setBackground(new Color(41, 128, 185));
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("📊 תוצאות סופיות: " + survey.getTopic(), SwingConstants.RIGHT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        long totalParticipants = survey.getParticipants().size();
        long completedParticipants = survey.getParticipants().values().stream()
                .filter(models.ParticipantState::isCompleted)
                .count();

        JLabel statsLabel = new JLabel(
                String.format("סה\"כ משתתפים: %d  |  השלימו את הסקר: %d (%.1f%%)",
                        totalParticipants, completedParticipants,
                        totalParticipants > 0 ? ((double) completedParticipants / totalParticipants) * 100 : 0.0),
                SwingConstants.RIGHT);
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        statsLabel.setForeground(new Color(236, 240, 241));

        headerPanel.add(titleLabel);
        headerPanel.add(statsLabel);
        add(headerPanel, BorderLayout.NORTH);

        // פאנל מרכזי נגלל לשאלות ולתוצאות
        JPanel questionsContainerPanel = new JPanel();
        questionsContainerPanel.setLayout(new BoxLayout(questionsContainerPanel, BoxLayout.Y_AXIS));
        questionsContainerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        List<Survey.Question> questions = survey.getQuestions();
        for (int i = 0; i < questions.size(); i++) {
            Survey.Question q = questions.get(i);
            questionsContainerPanel.add(createQuestionResultPanel(i + 1, q));
            questionsContainerPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        }

        JScrollPane scrollPane = new JScrollPane(questionsContainerPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        add(scrollPane, BorderLayout.CENTER);

        // פאנל תחתון לסגירה
        JPanel bottomPanel = new JPanel();
        JButton closeBtn = new JButton("סגור חלון");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        closeBtn.addActionListener(e -> dispose());
        bottomPanel.add(closeBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createQuestionResultPanel(int questionNum, Survey.Question question) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(Color.WHITE);

        // כותרת השאלה
        JLabel qLabel = new JLabel("שאלה " + questionNum + ": " + question.getText(), SwingConstants.RIGHT);
        qLabel.setFont(new Font("Arial", Font.BOLD, 15));
        qLabel.setForeground(new Color(44, 62, 80));
        card.add(qLabel, BorderLayout.NORTH);

        // רשימת התשובות הממוינות
        List<Survey.OptionResult> sortedResults = question.getSortedResults();
        JPanel optionsPanel = new JPanel(new GridLayout(sortedResults.size(), 1, 5, 5));
        optionsPanel.setBackground(Color.WHITE);

        for (Survey.OptionResult res : sortedResults) {
            JPanel optionRow = new JPanel(new BorderLayout(10, 5));
            optionRow.setBackground(Color.WHITE);

            // טקסט התשובה ומספר הקולות
            JLabel optLabel = new JLabel(String.format("%s (%d הצבעות)", res.getOptionText(), res.getVoteCount()), SwingConstants.RIGHT);
            optLabel.setFont(new Font("Arial", Font.PLAIN, 13));
            optLabel.setPreferredSize(new Dimension(250, 20));

            // Progress Bar להצגה ויזואלית של האחוזים
            JProgressBar progressBar = new JProgressBar(0, 100);
            int pctInt = (int) Math.round(res.getPercentage());
            progressBar.setValue(pctInt);
            progressBar.setStringPainted(true);
            progressBar.setString(String.format("%.1f%%", res.getPercentage()));
            progressBar.setForeground(new Color(52, 152, 219));

            optionRow.add(optLabel, BorderLayout.EAST);
            optionRow.add(progressBar, BorderLayout.CENTER);

            optionsPanel.add(optionRow);
        }

        card.add(optionsPanel, BorderLayout.CENTER);
        return card;
    }
}