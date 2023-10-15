package net.easecation.playeractionrecorder.data;

import java.util.Objects;

public class ActionDataEntry {

    public static final String SPLIT_CHAR = String.valueOf(new char[]{(char)185, (char)215});

    private final long logtime;
    private final String username;
    private final int category;
    private final int event;
    private final String metadata;
    private final String rawdata;

    public ActionDataEntry(long logtime, String username, int category, int event, String metadata, String rawdata) {
        Objects.requireNonNull(username);
        this.logtime = logtime;
        this.username = username;
        this.category = category;
        this.event = event;
        this.metadata = metadata;
        this.rawdata = rawdata;
    }

    public long getLogTime() {
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
        return (metadata == null || metadata.equals("null")) ? null : metadata;
    }

    public String getRawData() {
        return (rawdata == null || rawdata.equals("null")) ? null : rawdata;
    }

    public String encode() {
        //boybook;1;1;timestamp;metadata;rawdata
        return String.join(SPLIT_CHAR, username, String.valueOf(category), String.valueOf(event), String.valueOf(logtime), metadata, rawdata);
    }

    public static ActionDataEntry decode(String raw) {
        //boybook;GAMING_LOBBY_JOIN;timestamp;metadata;rawdata
        String[] data = raw.split(ActionDataEntry.SPLIT_CHAR, 6);
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
