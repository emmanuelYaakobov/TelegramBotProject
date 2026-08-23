package gui;

import models.Survey;
import services.AIService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CreateSurveyDialog extends JDialog {
    private final AIService aiService = new AIService();

    private JComboBox<String> methodCombo;
    private JTextField topicField;
    private JSpinner delaySpinner;
    private JTextArea previewArea;

    private List<Survey.Question> generatedQuestions = null;
    private String finalTopic = "";
    private boolean approved = false;

    public CreateSurveyDialog(JFrame parent) {
        super(parent, "יצירת סקר חדש", true);

        setSize(520, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        initUI();
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        topPanel.add(new JLabel("שיטת יצירה:"));
        methodCombo = new JComboBox<>(new String[]{"יצירה אוטומטית (AI)", "יצירה ידנית"});
        topPanel.add(methodCombo);

        topPanel.add(new JLabel("נושא הסקר / פרטים:"));
        topicField = new JTextField();
        topPanel.add(topicField);

        topPanel.add(new JLabel("עיכוב בשליחה (בדקות):"));
        delaySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 60, 1));
        topPanel.add(delaySpinner);

        JButton generateBtn = new JButton("צור תצוגה מקדימה");
        topPanel.add(new JLabel(""));
        topPanel.add(generateBtn);

        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("תצוגה מקדימה של השאלות לאישור"));

        previewArea = new JTextArea();
        previewArea.setEditable(false);
        centerPanel.add(new JScrollPane(previewArea), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton approveAndSendBtn = new JButton("אשר ושלח סקר");
        JButton cancelBtn = new JButton("ביטול");

        approveAndSendBtn.setBackground(new Color(46, 204, 113));
        approveAndSendBtn.setForeground(Color.WHITE);

        approveAndSendBtn.addActionListener(e -> {
            if (generatedQuestions == null || generatedQuestions.isEmpty()) {
                JOptionPane.showMessageDialog(this, "יש ליצור תצוגה מקדימה תחילה!", "שגיאה", JOptionPane.ERROR_MESSAGE);
                return;
            }
            approved = true;
            dispose();
        });

        cancelBtn.addActionListener(e -> dispose());

        bottomPanel.add(approveAndSendBtn);
        bottomPanel.add(cancelBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        generateBtn.addActionListener(e -> {
            String method = (String) methodCombo.getSelectedItem();
            if ("יצירה אוטומטית (AI)".equals(method)) {
                String input = topicField.getText().trim();
                if (input.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "נא להזין נושא או פרטים לסקר.", "שגיאה", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                generateViaAI(input);
            } else {
                generateManual();
            }
        });
    }

    private void generateViaAI(String topic) {
        try {
            String jsonResponse = aiService.generateSurvey(topic);
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            finalTopic = jsonObject.get("topic").getAsString();
            JsonArray questionsArray = jsonObject.getAsJsonArray("questions");

            generatedQuestions = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            sb.append("נושא: ").append(finalTopic).append("\n\n");

            for (int i = 0; i < questionsArray.size(); i++) {
                JsonObject qObj = questionsArray.get(i).getAsJsonObject();
                int id = qObj.get("id").getAsInt();
                String text = qObj.get("text").getAsString();

                List<String> options = new ArrayList<>();
                JsonArray optionsArr = qObj.getAsJsonArray("options");
                sb.append("שאלה ").append(id).append(": ").append(text).append("\n");

                for (int j = 0; j < optionsArr.size(); j++) {
                    String opt = optionsArr.get(j).getAsString();
                    options.add(opt);
                    sb.append("  - ").append(opt).append("\n");
                }
                sb.append("\n");
                generatedQuestions.add(new Survey.Question(id, text, options));
            }

            previewArea.setText(sb.toString());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "שגיאה ביצירת הסקר מול ה-AI.", "שגיאה", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateManual() {
        ManualSurveyDialog manualDialog = new ManualSurveyDialog(this);
        manualDialog.setVisible(true);

        if (manualDialog.isApproved()) {
            finalTopic = manualDialog.getTopic();
            generatedQuestions = manualDialog.getCreatedQuestions();
            topicField.setText(finalTopic);

            StringBuilder sb = new StringBuilder();
            sb.append("נושא (ידני): ").append(finalTopic).append("\n\n");
            for (Survey.Question q : generatedQuestions) {
                sb.append("שאלה ").append(q.getId()).append(": ").append(q.getText()).append("\n");
                for (String opt : q.getOptions()) {
                    sb.append("  - ").append(opt).append("\n");
                }
                sb.append("\n");
            }
            previewArea.setText(sb.toString());
        }
    }

    public boolean isApproved() { return approved; }
    public String getFinalTopic() { return finalTopic; }
    public List<Survey.Question> getGeneratedQuestions() { return generatedQuestions; }
    public int getDelayMinutes() { return (Integer) delaySpinner.getValue(); }
}