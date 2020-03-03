package be.kuleuven.softdev.kupo.alarm;

import java.util.ArrayList;

// this is a model user class for the database

public class User {

    private String username, displayName, statusMessage, wakeUpText, timeDisplay, email, userId, image, wakingName, wakingImage;
    private boolean waking, woken;
    private ArrayList<String> friendlist, wakelist;

    public User() {
        // empty constructor for database
    }

    public User(String username, String displayName, String statusMessage,
                String wakeUpText, String timeDisplay, String email, String image, String userId,
                String wakingName, String wakingImage, boolean waking, boolean woken,
                ArrayList<String> friendlist, ArrayList<String> wakelist) {
        this.username = username;
        this.displayName = displayName;
        this.statusMessage = statusMessage;
        this.wakeUpText = wakeUpText;
        this.timeDisplay = timeDisplay;
        this.image = image;
        this.email = email;
        this.userId = userId;
        this.wakingName = wakingName;
        this.wakingImage = wakingImage;
        this.waking = waking;
        this.woken = woken;
        this.friendlist = friendlist;
        this.wakelist = wakelist;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getWakeUpText() {
        return wakeUpText;
    }

    public String getTimeDisplay() {
        return timeDisplay;
    }

    public String getEmail() {
        return email;
    }

    public String getUserId() {
        return userId;
    }

    public String getImage() {
        return image;
    }

    public String getStatusMessage() { return statusMessage; }

    public String getWakingName() { return wakingName; }

    public String getWakingImage() { return wakingImage; }

    public boolean getWaking() { return waking; }

    public boolean getWoken() { return woken; }

}
