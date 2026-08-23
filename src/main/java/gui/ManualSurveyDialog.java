package gui;

import models.Survey;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ManualSurveyDialog extends JDialog {

    private JTextField topicField;
    private JComboBox<Integer> qCountCombo;
    private JPanel questionsContainer;
    private final List<QuestionPanelComponents> questionComponentsList = new ArrayList<>();

    private String topic = "";
    private List<Survey.Question> createdQuestions = null;
    private boolean approved = false;

    // השינוי המרכזי: קבלת Window מאפשרת להעביר גם JDialog וגם JFrame ללא שגיאת טיפוסים
    public ManualSurveyDialog(Window parent) {
        super(parent, "יצירת סקר ידני", ModalityType.APPLICATION_MODAL);
        setSize(600, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        initUI();
    }

    private void initUI() {
        // פאנל עליון - הזנת נושא וכמות שאלות (1-3)
        JPanel topPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        topPanel.add(new JLabel("נושא הסקר:"));
        topicField = new JTextField();
        topPanel.add(topicField);

        topPanel.add(new JLabel("מספר שאלות בסקר (1-3):"));
        qCountCombo = new JComboBox<>(new Integer[]{1, 2, 3});
        topPanel.add(qCountCombo);

        add(topPanel, BorderLayout.NORTH);

        // פאנל נגלל דינמי להזנת השאלות והתשובות
        questionsContainer = new JPanel();
        questionsContainer.setLayout(new BoxLayout(questionsContainer, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(questionsContainer);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        add(scrollPane, BorderLayout.CENTER);

        // עדכון מבנה הטופס בעת שינוי כמות השאלות
        qCountCombo.addActionListener(e -> rebuildQuestionsForm());
        rebuildQuestionsForm();

        // פאנל תחתון - כפתורי פעולה
        JPanel bottomPanel = new JPanel();
        JButton saveBtn = new JButton("אישור ושמירה");
        JButton cancelBtn = new JButton("ביטול");

        saveBtn.setBackground(new Color(46, 204, 113));
        saveBtn.setForeground(Color.WHITE);

        saveBtn.addActionListener(e -> validateAndSave());
        cancelBtn.addActionListener(e -> dispose());

        bottomPanel.add(saveBtn);
        bottomPanel.add(cancelBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void rebuildQuestionsForm() {
        questionsContainer.removeAll();
        questionComponentsList.clear();

        int count = (Integer) qCountCombo.getSelectedItem();
        for (int i = 1; i <= count; i++) {
            QuestionPanelComponents qComp = createQuestionPanel(i);
            questionComponentsList.add(qComp);
            questionsContainer.add(qComp.panel);
            questionsContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        questionsContainer.revalidate();
        questionsContainer.repaint();
    }

    private QuestionPanelComponents createQuestionPanel(int qNum) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "שאלה מספר " + qNum,
                TitledBorder.RIGHT,
                TitledBorder.TOP
        ));

        // טקסט השאלה
        JPanel qTextPanel = new JPanel(new BorderLayout(5, 5));
        qTextPanel.add(new JLabel("נוסח השאלה: "), BorderLayout.EAST);
        JTextField qTextField = new JTextField();
        qTextPanel.add(qTextField, BorderLayout.CENTER);
        panel.add(qTextPanel);

        // כמות תשובות (2-4)
        JPanel optCountPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        optCountPanel.add(new JLabel("מספר אפשרויות תשובה (2-4): "));
        JComboBox<Integer> optCombo = new JComboBox<>(new Integer[]{2, 3, 4});
        optCountPanel.add(optCombo);
        panel.add(optCountPanel);

        JPanel optionsContainer = new JPanel();
        optionsContainer.setLayout(new BoxLayout(optionsContainer, BoxLayout.Y_AXIS));
        panel.add(optionsContainer);

        List<JTextField> optionTextFields = new ArrayList<>();

        Runnable rebuildOptions = () -> {
            optionsContainer.removeAll();
            optionTextFields.clear();
            int numOpts = (Integer) optCombo.getSelectedItem();
            for (int j = 1; j <= numOpts; j++) {
                JPanel optRow = new JPanel(new BorderLayout(5, 5));
                optRow.add(new JLabel("  תשובה " + j + ": "), BorderLayout.EAST);
                JTextField optField = new JTextField();
                optionTextFields.add(optField);
                optRow.add(optField, BorderLayout.CENTER);
                optionsContainer.add(optRow);
            }
            optionsContainer.revalidate();
            optionsContainer.repaint();
        };

        optCombo.addActionListener(e -> rebuildOptions.run());
        rebuildOptions.run();

        return new QuestionPanelComponents(panel, qTextField, optionTextFields, qNum);
    }

    private void validateAndSave() {
        topic = topicField.getText().trim();
        if (topic.isEmpty()) {
            JOptionPane.showMessageDialog(this, "נא להזין נושא לסקר.", "שגיאה", JOptionPane.ERROR_MESSAGE);
            return;
        }

        createdQuestions = new ArrayList<>();

        for (QuestionPanelComponents qComp : questionComponentsList) {
            String qText = qComp.qTextField.getText().trim();
            if (qText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "נא למלא את נוסח שאלה " + qComp.qNum, "שגיאה", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<String> options = new ArrayList<>();
            for (JTextField optField : qComp.optionFields) {
                String optText = optField.getText().trim();
                if (optText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "נא למלא את כל אפשרויות התשובה בשאלה " + qComp.qNum, "שגיאה", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                options.add(optText);
            }

            createdQuestions.add(new Survey.Question(qComp.qNum, qText, options));
        }

        approved = true;
        dispose();
    }

    public boolean isApproved() { return approved; }
    public String getTopic() { return topic; }
    public List<Survey.Question> getCreatedQuestions() { return createdQuestions; }

    private static class QuestionPanelComponents {
        JPanel panel;
        JTextField qTextField;
        List<JTextField> optionFields;
        int qNum;

        QuestionPanelComponents(JPanel panel, JTextField qTextField, List<JTextField> optionFields, int qNum) {
            this.panel = panel;
            this.qTextField = qTextField;
            this.optionFields = optionFields;
            this.qNum = qNum;
        }
    }
}