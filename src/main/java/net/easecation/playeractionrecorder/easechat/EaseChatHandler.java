package net.easecation.playeractionrecorder.easechat;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import net.easecation.easechat.api.Logger;
import net.easecation.easechat.api.message.AutoSubChannelMessage;
import net.easecation.easechat.network.EaseChatClient;
import net.easecation.playeractionrecorder.PlayerActionRecorder;
import net.easecation.playeractionrecorder.TextFormat;
import net.easecation.playeractionrecorder.data.ActionDataEntry;
import net.easecation.playeractionrecorder.data.ChatLogEntry;
import net.easecation.playeractionrecorder.provider.C3p0ConnectionPool;
import net.easecation.playeractionrecorder.provider.MySQLDataProvider;
import net.easecation.playeractionrecorder.provider.ProviderException;

import java.net.URI;
import java.sql.SQLException;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            instance.registerChannels();
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

    private void registerChannels() {
        registerChannel("recorder");
        registerChannel("buglet");
        registerChannel("helper");
        registerChannel("stage-chat");
        registerChannel("lobby/main");
        registerChannel("lobby/mw");
        registerChannel("lobby/cw");
        registerChannel("lobby/parkour");
        registerChannel("lobby/pit");
        registerChannel("lobby/bedwars");
        registerChannel("lobby/sw");
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
                registerChannels();
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
                    try {
                        if (recv.getChannelName().equals("recorder")) {
                            handleRecorder(recv.getText());
                        } else if (recv.getChannelName().equals("buglet")) {
                            handleBuglet(recv.getText());
                        } else if (recv.getChannelName().equals("helper")) {
                            handleHelper(recv.getText());
                        } else if (recv.getChannelName().startsWith("lobby/")) {
                            String[] lobbyTypeData = recv.getChannelName().split("/", 2);
                            if (lobbyTypeData.length >= 2) {
                                String lobbyType = lobbyTypeData[1];
                                this.handleLobbyMessage(lobbyType, recv.getText());
                            }
                        } else if (recv.getChannelName().equals("stage-chat")) {
                            handleStageMessage(recv.getText());
                        }
                    } catch (Exception e) {
                        client.getLogger().error(e.getMessage());
                        e.printStackTrace();
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

    Queue<ActionDataEntry> queueAction = new LinkedBlockingQueue<>();
    long lastUpdateAction = 0;

    Queue<ChatLogEntry> queueChatLog = new LinkedBlockingQueue<>();
    long lastUpdateChatLog = 0;

    private void handleRecorder(String raw) {
        ActionDataEntry data = ActionDataEntry.decode(raw);
        if (data != null) {
            PlayerActionRecorder.getLogger().fine(data.toString());
            offerQueueAction(data);
            //MySQLDataProvider.getInstance().pushRecord(data);
        }
    }

    private void handleBuglet(String msg) {
        String[] data = msg.split("!\\$\\$!", 3);
        if (data.length >= 3) {
            String nick = data[0];
            String name = data[1];
            String message = data[2];
            PlayerActionRecorder.getLogger().warning("[BUGLET] " + name + " => " + message);
            //玩家名<大厅名 #id>
            Pattern r = Pattern.compile("(.*)<(.*) #(.*)>");
            Matcher m = r.matcher(TextFormat.clean(name));
            if (m.find()) {
                PlayerActionRecorder.getLogger().warning("[BUGLET Match] " + m.group(1) + " " + m.group(2) + " " + m.group(3));
                this.offerQueueChatLog(new ChatLogEntry(ChatLogEntry.Type.BUGLET, m.group(2), Integer.parseInt(m.group(3)), nick, m.group(1), TextFormat.clean(message)));
            }
        }
    }

    private void handleLobbyMessage(String lobbyType, String msg) {
        String[] sp = msg.split("!\\$\\$!");
        if (sp.length >= 6) {
            String lobbyIdentifier = sp[0];
            int id = Integer.parseInt(sp[1]);
            String nickName = sp[2];
            String showedName = sp[3];
            String aliasName = sp[4];
            String message = sp[5];
            PlayerActionRecorder.getLogger().warning("[LOBBY] " + lobbyType + "," + lobbyIdentifier + "," + id + "," + showedName + "," + aliasName + " => " + message);
            this.offerQueueChatLog(new ChatLogEntry(ChatLogEntry.Type.LOBBY, lobbyType, id, nickName, aliasName, TextFormat.clean(message)));
        }
    }

    private void handleHelper(String msg) {
        String[] data = msg.split("!\\$\\$!", 3);
        if (data.length >= 3) {
            String nick = data[0];
            String name = data[1];
            String message = data[2];
            if (!name.equals("WAntiCheatPro#作弊检测系统")) {
                PlayerActionRecorder.getLogger().warning("[HELPER] " + name + " => " + message);
                this.offerQueueChatLog(new ChatLogEntry(ChatLogEntry.Type.HELPER, "", 0, nick, name, TextFormat.clean(message)));
            }
        }
    }

    private void handleStageMessage(String msg) {
        String[] data = msg.split("!\\$\\$!", 8);
        if (data.length >= 8) {
            String stageType = data[0];
            int stageRuntimeId = Integer.parseInt(data[1]);
            int stageDBId = Integer.parseInt(data[2]);
            String playerNick = data[3];
            String playerDisplayName = data[4];
            String playerAliasName = data[5];
            String playerTeam = data[6];
            String message = data[7];
            PlayerActionRecorder.getLogger().warning("[STAGE] [" + stageType + "-" + stageRuntimeId + "]" + playerAliasName + " => " + message);
            this.offerQueueChatLog(new ChatLogEntry(ChatLogEntry.Type.STAGE, stageType, stageRuntimeId, playerNick, playerAliasName, TextFormat.clean(message)));
        }
    }

    //======================================

    private void offerQueueAction(ActionDataEntry data) {
        queueAction.offer(data);
    }

    private void pushQueueAction() throws ProviderException {
        if (queueAction.isEmpty()) return;
        ActionDataEntry[] push = queueAction.toArray(new ActionDataEntry[0]);
        queueAction.clear();
        PlayerActionRecorder.getLogger().warning("[RecordAction] 正在上传 " + push.length + " 条数据...");
        MySQLDataProvider.getInstance().pushRecords(push);
        insertIn5Seconds += push.length;
        lastUpdateAction = System.currentTimeMillis();
    }

    public void tryPushQueueAction() {
        if (queueAction.size() >= 100 || System.currentTimeMillis() > lastUpdateAction + 500) {
            try {
                this.pushQueueAction();
            } catch (ProviderException e) {
                e.printStackTrace();
            }
        }
    }

    private void offerQueueChatLog(ChatLogEntry data) {
        queueChatLog.offer(data);
    }

    private void pushQueueChatLog() {
        if (queueChatLog.isEmpty()) return;
        ChatLogEntry[] push = queueChatLog.toArray(new ChatLogEntry[0]);
        queueChatLog.clear();
        PlayerActionRecorder.getLogger().warning("[ChatLog] 正在上传 " + push.length + " 条数据...");
        try {
            MySQLDataProvider.getInstance().pushChatLog(push);
        } catch (ProviderException ignore) {}
        insertIn5Seconds += push.length;
        lastUpdateChatLog = System.currentTimeMillis();
    }

    public void tryPushQueueChatLog() {
        if (queueChatLog.size() >= 100 || System.currentTimeMillis() > lastUpdateChatLog + 500) {
            this.pushQueueChatLog();
        }
    }

}
