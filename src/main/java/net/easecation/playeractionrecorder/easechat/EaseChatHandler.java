package net.easecation.playeractionrecorder.easechat;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import net.easecation.easechat.api.Logger;
import net.easecation.easechat.api.message.AutoSubChannelMessage;
import net.easecation.easechat.network.EaseChatClient;
import net.easecation.playeractionrecorder.PlayerActionRecorder;
import net.easecation.playeractionrecorder.action.ActionDataEntry;
import net.easecation.playeractionrecorder.provider.C3p0ConnectionPool;
import net.easecation.playeractionrecorder.provider.MySQLDataProvider;
import net.easecation.playeractionrecorder.provider.ProviderException;

import java.net.URI;
import java.sql.SQLException;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

public class EaseChatHandler {

    private URI uri;
    private String clientName;
    private EaseChatClient client;
    private long nextAllowReconnect = 0;

    private static EaseChatHandler instance;

    public static EaseChatHandler getInstance() {
        return instance;
    }

    private int insertIn5Seconds = 0;

    public static void init(URI uri, String clientName) {
        instance = new EaseChatHandler();
        instance.uri = uri;
        instance.clientName = clientName;
        try {
            instance.connect(uri, clientName);
            instance.registerChannel("recorder");
        } catch (Exception e) {
            PlayerActionRecorder.getLogger().warning("[EaseChat] Failed to connect " + uri.toString());
            e.printStackTrace();
            PlayerActionRecorder.getLogger().warning("[EaseChat] Try again in 5 seconds");
            instance.nextAllowReconnect = System.currentTimeMillis() + 5000;
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                instance.onUpdate();
            }
        }, 0, 5000);
    }

    public void shutdown() {
        if (this.client != null) this.client.shutdown();
    }

    public void onUpdate() {
        if (insertIn5Seconds > 0) {
            try {
                ComboPooledDataSource ds = C3p0ConnectionPool.getInstance().getDs();
                PlayerActionRecorder.getLogger().info("Insert " + insertIn5Seconds + " records in five seconds!\n **** database connection data: current=" + ds.getNumConnections()
                        + " busy=" + ds.getNumBusyConnections()
                        + " idle=" + ds.getNumIdleConnections());
            } catch (SQLException e) {
                //ignore
            }
            insertIn5Seconds = 0;
        }
        if (this.client == null || !this.client.isActive()) {
            if (System.currentTimeMillis() < this.nextAllowReconnect) return;
            this.nextAllowReconnect = Long.MAX_VALUE;
            PlayerActionRecorder.getLogger().warning("[EaseChat] Connection of " + uri.toString() + " disconnected! Reconnecting~");

            try {
                connect(uri, clientName);
                client.getLogger().info("Reregister channels...");
                registerChannel("recorder");
            } catch (Exception e) {
                PlayerActionRecorder.getLogger().warning("[EaseChat] Failed to connect " + uri.toString());
                e.printStackTrace();
                PlayerActionRecorder.getLogger().warning("[EaseChat] Try again in 5 seconds");
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

    Queue<ActionDataEntry> queue = new LinkedBlockingQueue<>();
    long lastUpdate = 0;

    private void handle(String raw) {
        ActionDataEntry data = ActionDataEntry.decode(raw);
        if (data != null) {
            PlayerActionRecorder.getLogger().fine(data.toString());
            offerQueue(data);
            //MySQLDataProvider.getInstance().pushRecord(data);
        }
    }

    private void offerQueue(ActionDataEntry data) {
        queue.offer(data);
        if (queue.size() >= 100 || System.currentTimeMillis() > lastUpdate + 500) {
            try {
                this.pushQueue();
            } catch (ProviderException e) {
                e.printStackTrace();
            }
        }
    }

    private void pushQueue() throws ProviderException {
        if (queue.isEmpty()) return;
        ActionDataEntry[] push = queue.toArray(new ActionDataEntry[0]);
        queue.clear();
        PlayerActionRecorder.getLogger().warning("正在上传 " + push.length + " 条数据...");
        MySQLDataProvider.getInstance().pushRecords(push);
        insertIn5Seconds+= push.length;
        lastUpdate = System.currentTimeMillis();
    }

    public void registerChannel(String channel) {
        if (client == null || client.getSender() == null) {
            new EaseChatSentFailedException("Not connected").printStackTrace();
            return;
        }
        client.getSender().sendAsyncChannelMessage(new AutoSubChannelMessage(channel), f -> {
            if (f.isSuccess()) {
                client.getLogger().info("Channel " + channel + " registered!");
            }
        });
    }

}
