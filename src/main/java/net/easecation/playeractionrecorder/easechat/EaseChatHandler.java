package net.easecation.playeractionrecorder.easechat;

import net.easecation.easechat.api.Logger;
import net.easecation.easechat.api.message.AutoSubChannelMessage;
import net.easecation.easechat.network.EaseChatClient;
import net.easecation.playeractionrecorder.PlayerActionRecorder;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

public class EaseChatHandler {

    private URI uri;
    private String clientName;
    private EaseChatClient client;
    private long nextAllowReconnect = 0;

    private static EaseChatHandler instance;

    public static EaseChatHandler getInstance() {
        return instance;
    }

    public static void init(URI uri, String clientName) {
        instance = new EaseChatHandler();
        instance.uri = uri;
        instance.clientName = clientName;
        try {
            instance.connect(uri, clientName);
            instance.registerChannel("recorder");
        } catch (Exception e) {
            PlayerActionRecorder.getLogger().warning("[EaseChat] 连接服务端 " + uri.toString() + " 时失败！");
            e.printStackTrace();
            PlayerActionRecorder.getLogger().warning("[EaseChat] 5秒后将重试！");
            instance.nextAllowReconnect = System.currentTimeMillis() + 5000;
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                instance.onUpdate();
            }
        }, 5000);
    }

    public void shutdown() {
        if (this.client != null) this.client.shutdown();
    }

    public void onUpdate() {
        if (this.client == null || !this.client.isActive()) {
            if (System.currentTimeMillis() < this.nextAllowReconnect) return;
            this.nextAllowReconnect = Long.MAX_VALUE;
            PlayerActionRecorder.getLogger().warning("[EaseChat] 发现已断开与 " + uri.toString() + " 的连接！正在重新连接~");

            try {
                connect(uri, clientName);
                client.getLogger().info("正在重新订阅频道...");
                registerChannel("recorder");
            } catch (Exception e) {
                PlayerActionRecorder.getLogger().warning("[EaseChat] 重新连接服务端 " + uri.toString() + " 时失败！");
                e.printStackTrace();
                PlayerActionRecorder.getLogger().warning("[EaseChat] 5秒后将重试！");
                client.shutdown();
                nextAllowReconnect = System.currentTimeMillis() + 5000;
            }
        }
    }

    private void connect(URI uri, String clientName) throws Exception {
        PlayerActionRecorder.getLogger().info("[EaseChat] Connecting EaseChat server...");
        client = new EaseChatClient(
                clientName, uri,
                recv -> {
                    if (recv.getChannelName().equals("recorder")) {
                        handle(recv.getText());
                    }
                }
        );
        client.setLogger(new Logger() {
            @Override
            public void emergency(String message) {
                PlayerActionRecorder.getLogger().severe("[EaseChat] " + message);
            }
            @Override
            public void alert(String message) {
                PlayerActionRecorder.getLogger().warning("[EaseChat] " + message);
            }
            @Override
            public void critical(String message) {
                PlayerActionRecorder.getLogger().warning("[EaseChat] " + message);
            }
            @Override
            public void error(String message) {
                PlayerActionRecorder.getLogger().severe("[EaseChat] " + message);
            }
            @Override
            public void warning(String message) {
                PlayerActionRecorder.getLogger().warning("[EaseChat] " + message);
            }
            @Override
            public void notice(String message) {
                PlayerActionRecorder.getLogger().info("[EaseChat] " + message);
            }
            @Override
            public void info(String message) {
                PlayerActionRecorder.getLogger().info("[EaseChat] " + message);
            }
            @Override
            public void debug(String message) {
                PlayerActionRecorder.getLogger().fine("[EaseChat] " + message);
            }
        });
        client.start();
        client.getLogger().info("EaseChat server connected!" + uri);
    }

    private void handle(String data) {

    }

    public void registerChannel(String channel) {
        if (client == null || client.getSender() == null) {
            new EaseChatSentFailedException("Not connected").printStackTrace();
            return;
        }
        client.getSender().sendAsyncChannelMessage(new AutoSubChannelMessage(channel), f -> {
            if (f.isSuccess()) {
                client.getLogger().info("已注册频道 " + channel);
            }
        });
    }

}
