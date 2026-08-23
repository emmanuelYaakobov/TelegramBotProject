package models;

import java.time.LocalTime;

public class User {
    private final long chatId;
    private final String firstName;
    private final String lastName;
    private final String username;
    private final LocalTime joinTime;

    public User(long chatId, String firstName, String lastName, String username) {
        this.chatId = chatId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.joinTime = LocalTime.now(); // מועד ההצטרפות נשמר אוטומטית
    }

    // Getters
    public long getChatId() { return chatId; }
    public String getFullName() {
        return (lastName != null) ? firstName + " " + lastName : firstName;
    }
    public String getUsername() { return username; }
    public LocalTime getJoinTime() { return joinTime; }
}