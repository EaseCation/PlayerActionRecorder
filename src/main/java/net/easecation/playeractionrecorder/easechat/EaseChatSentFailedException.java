package net.easecation.playeractionrecorder.easechat;

public class EaseChatSentFailedException extends RuntimeException {

    public EaseChatSentFailedException() {
    }

    public EaseChatSentFailedException(String message) {
        super(message);
    }

    public EaseChatSentFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public EaseChatSentFailedException(Throwable cause) {
        super(cause);
    }

    public EaseChatSentFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
