package net.easecation.playeractionrecorder.action;

import java.util.Objects;

public class ActionDataEntry {

    public static final String SPLIT_CHAR = String.valueOf(new char[]{(char)185, (char)215});

    private long logtime;
    private String username;
    private int category;
    private int event;
    private String metadata;
    private String rawdata;

    public ActionDataEntry(long logtime, String username, int category, int event, String metadata, String rawdata) {
        Objects.requireNonNull(username);
        this.logtime = logtime;
        this.username = username;
        this.category = category;
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

    public int getCategory() {
        return category;
    }

    public int getEvent() {
        return event;
    }

    public String getMetadata() {
        return metadata;
    }

    public String getRawdata() {
        return rawdata;
    }

    public String encode() {
        //boybook;1;1;timestamp;metadata;rawdata
        return String.join(SPLIT_CHAR, username, String.valueOf(category), String.valueOf(event), String.valueOf(logtime), metadata, rawdata);
    }

    public static ActionDataEntry decode(String raw) {
        //boybook;GAMING_LOBBY_JOIN;timestamp;metadata;rawdata
        String[] data = raw.split(ActionDataEntry.SPLIT_CHAR, 5);
        if (data.length == 6) {
            try {
                int category = Integer.parseInt(data[1]);
                int event = Integer.parseInt(data[2]);
                long timestamp = Long.parseLong(data[3]);
                return new ActionDataEntry(timestamp, data[0], category, event, data[4], data[5]);
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
