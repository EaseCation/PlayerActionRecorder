package net.easecation.playeractionrecorder.action;

import java.util.Objects;

public class ActionDataEntry {

    public static final String SPLIT_CHAR = String.valueOf(new char[]{(char)185, (char)215});

    private long logtime;
    private String username;
    private ActionCategory category;
    private ActionEvent event;
    private String metadata;
    private String rawdata;

    public ActionDataEntry(long logtime, String username, ActionEvent event, String metadata, String rawdata) {
        Objects.requireNonNull(username);
        Objects.requireNonNull(event);
        this.logtime = logtime;
        this.username = username;
        this.category = event.getCategory();
        this.event = event;
        this.metadata = metadata;
        this.rawdata = rawdata;
    }

    public long getLogtime() {
        return logtime;
    }

    public String getUsername() {
        return username;
    }

    public ActionCategory getCategory() {
        return category;
    }

    public ActionEvent getEvent() {
        return event;
    }

    public String getMetadata() {
        return metadata;
    }

    public String getRawdata() {
        return rawdata;
    }

    public String encode() {
        //boybook;GAMING_LOBBY_JOIN;timestamp;metadata;rawdata
        return String.join(SPLIT_CHAR, username, event.name(), String.valueOf(logtime), metadata, rawdata);
    }

    public static ActionDataEntry decode(String raw) {
        //boybook;GAMING_LOBBY_JOIN;timestamp;metadata;rawdata
        String[] data = raw.split(ActionDataEntry.SPLIT_CHAR, 5);
        if (data.length == 5) {
            try {
                ActionEvent action = ActionEvent.valueOf(data[1]);
                long timestamp = Long.parseLong(data[2]);
                return new ActionDataEntry(timestamp, data[0], action, data[3], data[4]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "ActionDataEntry{" +
                "logtime=" + logtime +
                ", username='" + username + '\'' +
                ", category=" + category +
                ", event=" + event +
                ", metadata='" + metadata + '\'' +
                ", rawdata='" + rawdata + '\'' +
                '}';
    }
}
