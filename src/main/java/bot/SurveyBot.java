package bot;

import manager.SystemManager;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class SurveyBot extends TelegramLongPollingBot {

    private final SystemManager systemManager;

    public SurveyBot(SystemManager manager) {
        this.systemManager = manager;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText().trim();
            long chatId = update.getMessage().getChatId();

            if (text.equalsIgnoreCase("/start") || text.equals("היי") || text.equalsIgnoreCase("hi")) {
                String firstName = update.getMessage().getFrom().getFirstName();
                String lastName = update.getMessage().getFrom().getLastName();
                String username = update.getMessage().getFrom().getUserName();

                systemManager.registerNewUser(chatId, firstName, lastName, username);
            }
        } else if (update.hasPollAnswer()) {
            long userId = update.getPollAnswer().getUser().getId();
            List<Integer> selectedOptions = update.getPollAnswer().getOptionIds();

            if (!selectedOptions.isEmpty()) {
                int selectedOptionIndex = selectedOptions.get(0);
                systemManager.handlePollAnswer(userId, selectedOptionIndex);
            }
        }
    }

    public void sendQuestionAsPoll(long chatId, String questionText, List<String> options) {
        SendPoll sendPoll = new SendPoll();
        sendPoll.setChatId(String.valueOf(chatId));
        sendPoll.setQuestion(questionText);
        sendPoll.setOptions(options);
        sendPoll.setType("regular");
        sendPoll.setIsAnonymous(false);

        try {
            execute(sendPoll);
        } catch (TelegramApiException e) {
            System.err.println("שגיאה בשליחת סקר ל-" + chatId + ": " + e.getMessage());
        }
    }

    public void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "SurveyManagementBot";
    }

    @Override
    public String getBotToken() {
        return "8849244322:AAFIJz9Uhb9S_Zf4QhNv5E9xDXPqdpwY1Yw";
    }
}