package gui;

import manager.SystemManager;
import models.ParticipantState;
import models.Survey;
import models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class DashboardFrame extends JFrame {

    private final SystemManager systemManager;

    private JTable communityTable;
    private DefaultTableModel communityTableModel;
    private JLabel communityCountLabel;

    private JTable surveyTable;
    private DefaultTableModel surveyTableModel;
    private JLabel surveyStatusLabel;
    private JLabel surveySummaryLabel;

    public DashboardFrame(SystemManager manager) {
        this.systemManager = manager;

        setTitle("מערכת ניהול סקרים - Dashboard");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel communityPanel = createCommunityPanel();
        tabbedPane.addTab("👥 חברי קהילה", communityPanel);

        JPanel activeSurveyPanel = createActiveSurveyPanel();
        tabbedPane.addTab("📊 סקר פעיל ומושהה", activeSurveyPanel);

        add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton createSurveyBtn = new JButton("➕ צור סקר חדש");
        createSurveyBtn.setFont(new Font("Arial", Font.BOLD, 14));
        createSurveyBtn.setBackground(new Color(41, 128, 185));
        createSurveyBtn.setForeground(Color.WHITE);
        createSurveyBtn.setFocusPainted(false);

        createSurveyBtn.addActionListener(e -> {
            if (systemManager.getCommunity().size() < 3) {
                JOptionPane.showMessageDialog(this,
                        "לא ניתן להתחיל סקר. יש להמתין שלפחות 3 חברים יצטרפו לקהילה.",
                        "שגיאה בהפעלת סקר",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            CreateSurveyDialog dialog = new CreateSurveyDialog(this);
            dialog.setVisible(true);

            if (dialog.isApproved()) {
                boolean created = systemManager.createCustomSurvey(
                        dialog.getFinalTopic(),
                        dialog.getGeneratedQuestions(),
                        dialog.getDelayMinutes()
                );

                if (created) {
                    if (dialog.getDelayMinutes() > 0) {
                        JOptionPane.showMessageDialog(this,
                                "הסקר נוצר בהצלחה והושהה ל-" + dialog.getDelayMinutes() + " דקות. הספירה לאחור החלה!",
                                "סקר מושהה",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "הסקר אושר ונשלח כעת למשתתפים!",
                                "סקר נשלח",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        bottomPanel.add(createSurveyBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createCommunityPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(236, 240, 241));
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("רשימת חברי הקהילה הכללית");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        communityCountLabel = new JLabel("סה\"כ חברים: 0");
        communityCountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        communityCountLabel.setForeground(new Color(44, 62, 80));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(communityCountLabel, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);

        String[] columnNames = {"Chat ID", "מועד הצטרפות", "שם מלא", "שם משתמש"};
        communityTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        communityTable = new JTable(communityTableModel);
        communityTable.setRowHeight(24);
        centerTableText(communityTable);

        JScrollPane scrollPane = new JScrollPane(communityTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createActiveSurveyPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topContainer = new JPanel(new GridLayout(2, 1, 5, 5));
        topContainer.setBackground(new Color(236, 240, 241));
        topContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        surveyStatusLabel = new JLabel("אין סקר פעיל כרגע", SwingConstants.CENTER);
        surveyStatusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        surveyStatusLabel.setForeground(new Color(44, 62, 80));

        surveySummaryLabel = new JLabel("משתתפים: 0 | השלימו: 0 | טרם השלימו: 0", SwingConstants.CENTER);
        surveySummaryLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        surveySummaryLabel.setForeground(new Color(127, 140, 141));

        topContainer.add(surveyStatusLabel);
        topContainer.add(surveySummaryLabel);
        panel.add(topContainer, BorderLayout.NORTH);

        String[] columnNames = {"שם משתמש", "שאלות שנענו", "מתוך", "סטטוס"};
        surveyTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        surveyTable = new JTable(surveyTableModel);
        surveyTable.setRowHeight(24);
        centerTableText(surveyTable);

        JScrollPane scrollPane = new JScrollPane(surveyTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void centerTableText(JTable table) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int x = 0; x < table.getColumnCount(); x++) {
            table.getColumnModel().getColumn(x).setCellRenderer(centerRenderer);
        }
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
    }

    public void refreshCommunityTable() {
        SwingUtilities.invokeLater(() -> {
            communityTableModel.setRowCount(0);
            Map<Long, User> community = systemManager.getCommunity();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            for (User user : community.values()) {
                Object[] rowData = {
                        user.getChatId(),
                        user.getJoinTime().format(formatter),
                        user.getFullName(),
                        (user.getUsername() != null && !user.getUsername().isEmpty()) ? "@" + user.getUsername() : "אין"
                };
                communityTableModel.addRow(rowData);
            }

            communityCountLabel.setText("סה\"כ חברים: " + community.size());
        });
    }

    public void refreshActiveSurveyTable() {
        SwingUtilities.invokeLater(() -> {
            surveyTableModel.setRowCount(0);
            Survey activeSurvey = systemManager.getActiveSurvey();

            surveyStatusLabel.setText(systemManager.getFormattedTimerString());

            if (activeSurvey == null || !activeSurvey.isActive()) {
                if (!systemManager.isDelayed()) {
                    surveySummaryLabel.setText("משתתפים: 0 | השלימו: 0 | טרם השלימו: 0");
                }
                return;
            }

            Map<Long, ParticipantState> participants = activeSurvey.getParticipants();
            Map<Long, User> community = systemManager.getCommunity();

            int totalParticipants = participants.size();
            int completedCount = 0;

            for (Map.Entry<Long, ParticipantState> entry : participants.entrySet()) {
                long chatId = entry.getKey();
                ParticipantState state = entry.getValue();
                User user = community.get(chatId);

                if (state.isCompleted()) {
                    completedCount++;
                }

                String name = (user != null) ? user.getFullName() : String.valueOf(chatId);

                Object[] rowData = {
                        name,
                        state.getAnsweredCount(),
                        activeSurvey.getTotalQuestions(),
                        state.getStatusString()
                };
                surveyTableModel.addRow(rowData);
            }

            int pendingCount = totalParticipants - completedCount;
            surveySummaryLabel.setText(String.format("משתתפים בסקר: %d | השלימו: %d | טרם השלימו: %d",
                    totalParticipants, completedCount, pendingCount));
        });
    }
}