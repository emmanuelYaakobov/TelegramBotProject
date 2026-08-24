package org.example;

import bot.SurveyBot;
import gui.DashboardFrame;
import manager.SystemManager;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        try {
            SystemManager systemManager = new SystemManager();
            SurveyBot myBot = new SurveyBot(systemManager);

            // השורה החדשה: מעבירים למנהל המערכת את הבוט כדי שיוכל לשלוח דרכו
            systemManager.setBot(myBot);

            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(myBot);
            System.out.println("הבוט פועל ומאזין להודעות...");

            // הפעלת ממשק המשתמש וחיבורו למנהל המערכת
            SwingUtilities.invokeLater(() -> {
                DashboardFrame dashboard = new DashboardFrame(systemManager);
                systemManager.setDashboardFrame(dashboard);
                dashboard.setVisible(true);
            });

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
