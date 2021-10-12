package net.easecation.playeractionrecorder;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import net.easecation.playeractionrecorder.easechat.EaseChatHandler;
import net.easecation.playeractionrecorder.provider.C3p0ConnectionPool;
import net.easecation.playeractionrecorder.provider.MySQLDataProvider;

import java.io.*;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class PlayerActionRecorder {

    private static Logger logger = Logger.getLogger(PlayerActionRecorder.class.getName());
    public final static String PATH = System.getProperty("user.dir") + "/";

    public static PlayerActionRecorder INSTANCE;

    public static Logger getLogger() {
        return logger;
    }

    public static void main(String[] args) {
        logger.info("Starting player action recorder...");

        ComboPooledDataSource cpds = null;
        URI easechat = null;
        try {
            Properties props = new Properties();
            File file = new File(PATH, "config.properties");
            if (!file.isFile()) {
                InputStream in = PlayerActionRecorder.class.getClassLoader().getResourceAsStream("config.properties");
                InputStreamReader reader = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(reader);
                file.createNewFile();
                BufferedWriter out = new BufferedWriter(new FileWriter(file));
                String line;
                while ((line = br.readLine()) != null) {
                    out.write(line);
                    out.newLine();
                }
                out.flush();
                out.close();
            }

            InputStream in = new FileInputStream(file);
            props.load(in);
            in.close();

            cpds = new ComboPooledDataSource();
            cpds.setDriverClass(props.getProperty("driverClass"));
            cpds.setJdbcUrl(props.getProperty("jdbcUrl"));
            cpds.setUser(props.getProperty("user"));
            cpds.setPassword(props.getProperty("password"));
            cpds.setInitialPoolSize(Integer.parseInt(props.getProperty("initialPoolSize")));
            cpds.setMaxIdleTime(Integer.parseInt(props.getProperty("maxIdleTime")));
            cpds.setMaxPoolSize(Integer.parseInt(props.getProperty("maxPoolSize")));
            cpds.setMinPoolSize(Integer.parseInt(props.getProperty("minPoolSize")));

            easechat = new URI(props.getProperty("easechat"));
        } catch (Exception e) {
            logger.warning("Failed to loadConfig.");
            e.printStackTrace();
            System.exit(0);
        }

        try {
            MySQLDataProvider.load(cpds);
            logger.finest("Database connect successful!");
        } catch (Exception e) {
            logger.warning("Failed to connect to server.");
            e.printStackTrace();
            System.exit(0);
        }

        try {
            EaseChatHandler.init(easechat, "recorder");
            logger.finest("EaseChat connect successful!");
        } catch (Exception e) {
            logger.warning("Failed to connect to EaseChat.");
            e.printStackTrace();
            System.exit(0);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                getLogger().info("Closing database...");
                C3p0ConnectionPool.getInstance().shutdown();
                getLogger().info("Database closed!");

                getLogger().info("Closing easechat...");
                EaseChatHandler.getInstance().shutdown();
                getLogger().info("EaseChat closed!");

                INSTANCE.isRunning.set(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        INSTANCE = new PlayerActionRecorder();
    }

    // ====================================================================


    public PlayerActionRecorder() {
        this.tickProcessor();
    }

    private long nextTick;
    private AtomicBoolean isRunning = new AtomicBoolean(true);

    public void tickProcessor() {
        this.nextTick = System.currentTimeMillis();
        try {
            while (this.isRunning.get()) {
                try {
                    this.tick();
                } catch (RuntimeException e) {
                    getLogger().warning("tickProcessor ROOT RuntimeException");
                    e.printStackTrace();
                } finally {
                    long next = this.nextTick;
                    long current = System.currentTimeMillis();
                    if (next - 0.1 > current) {
                        Thread.sleep(next - current - 1, 900000);
                    }
                }
            }
        } catch (Throwable e) {
            getLogger().warning("Exception happened while ticking server");
            e.printStackTrace();
        }
    }

    public void tick() {
        EaseChatHandler.getInstance().tryPushQueueAction();
        EaseChatHandler.getInstance().tryPushQueueChatLog();
    }

}
