package net.easecation.playeractionrecorder.data;

public class ChatLogEntry {

    public enum Type {
        LOBBY, STAGE, BUGLET, HELPER, FRIEND_CHAT, GUILD_CHAT, PARTY_CHAT
    }

    private final Type type;
    private final String posType;
    private final int posId;
    private final String sourceNick;
    private final String sourceName;
    private final String message;

    public ChatLogEntry(Type type, String posType, int posId, String sourceNick, String sourceName, String message) {
        this.type = type;
        this.posType = posType;
        this.posId = posId;
        this.sourceNick = sourceNick;
        this.sourceName = sourceName;
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    public String getPosType() {
        return posType;
    }

    public int getPosId() {
        return posId;
    }

    public String getSourceNick() {
        return sourceNick;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getMessage() {
        return message;
    }
}
