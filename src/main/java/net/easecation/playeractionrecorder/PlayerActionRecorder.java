package net.easecation.playeractionrecorder;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import net.easecation.playeractionrecorder.easechat.EaseChatHandler;
import net.easecation.playeractionrecorder.provider.C3p0ConnectionPool;

import java.io.*;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class PlayerActionRecorder {

    private static Logger logger = Logger.getLogger(PlayerActionRecorder.class.getName());
    public final static String PATH = System.getProperty("user.dir") + "/";

    public static Logger getLogger() {
        return logger;
    }


    public static void main(String[] args) {
        logger.info("Starting player action recorder...");

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

            ComboPooledDataSource cpds = new ComboPooledDataSource();
            cpds.setDriverClass(props.getProperty("driverClass"));
            cpds.setJdbcUrl(props.getProperty("jdbcUrl"));
            cpds.setUser(props.getProperty("user"));
            cpds.setPassword(props.getProperty("password"));
            cpds.setInitialPoolSize(Integer.parseInt(props.getProperty("initialPoolSize")));
            cpds.setMaxIdleTime(Integer.parseInt(props.getProperty("maxIdleTime")));
            cpds.setMaxPoolSize(Integer.parseInt(props.getProperty("maxPoolSize")));
            cpds.setMinPoolSize(Integer.parseInt(props.getProperty("minPoolSize")));
            C3p0ConnectionPool.loadConfig(cpds);

            easechat = new URI(props.getProperty("easechat"));
        } catch (Exception e) {
            logger.warning("Failed to loadConfig.");
            e.printStackTrace();
            System.exit(0);
        }

        try {
            Connection connection = C3p0ConnectionPool.getInstance().getConnection();
            connection.close();
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

}
