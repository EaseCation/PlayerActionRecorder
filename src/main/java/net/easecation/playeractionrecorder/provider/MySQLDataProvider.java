package net.easecation.playeractionrecorder.provider;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import net.easecation.playeractionrecorder.data.ActionDataEntry;
import net.easecation.playeractionrecorder.data.ChatLogEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class MySQLDataProvider {

    private static MySQLDataProvider instance;

    public static MySQLDataProvider getInstance() {
        return instance;
    }

    public static void load(ComboPooledDataSource dataSource) {
        C3p0ConnectionPool.loadConfig(dataSource);
        instance = new MySQLDataProvider();
    }

    private MySQLDataProvider() {

    }

    private static Connection getConnection() {
        return C3p0ConnectionPool.getInstance().getConnection();
    }

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void pushRecords(ActionDataEntry[] records) throws ProviderException {
        try {
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO recorder.records (logtime, username, category, event, metadata, rawdata) VALUES (?, ?, ?, ?, ?, ?)"
            );
            for (ActionDataEntry record : records) {
                String time = Instant.ofEpochMilli(record.getLogTime()).atOffset(ZoneOffset.ofHours(8)).format(dtf);
                statement.setString(1, time);
                statement.setString(2, record.getUsername());
                statement.setInt(3, record.getCategory());
                statement.setInt(4, record.getEvent());
                statement.setString(5, record.getMetadata());
                statement.setString(6, record.getRawData());
                statement.addBatch();
            }

            statement.executeBatch();

            statement.close();
            connection.close();
        } catch (Exception e){
            throw new ProviderException("Exception caught when pushRecords:", e);
        }
    }

    public void pushChatLog(ChatLogEntry[] records) throws ProviderException {
        try {
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO recorder.logChat (type, posType, posId, sourceNick, sourceName, message) VALUES (?, ?, ?, ?, ?, ?)"
            );
            for (ChatLogEntry record : records) {
                statement.setString(1, record.getType().name());
                statement.setString(2, record.getPosType());
                statement.setInt(3, record.getPosId());
                statement.setString(4, record.getSourceNick());
                statement.setString(5, record.getSourceName());
                statement.setString(6, record.getMessage());
                statement.addBatch();
            }

            statement.executeBatch();

            statement.close();
            connection.close();
        } catch (Exception e){
            throw new ProviderException("Exception caught when pushChatLog:", e);
        }
    }

    public void pushRecord(ActionDataEntry record) throws ProviderException {
        try {
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO recorder.records (logtime, username, category, event, metadata, rawdata) VALUES (?, ?, ?, ?, ?, ?)"
            );
            statement.setTimestamp(1, new Timestamp(record.getLogTime()));
            statement.setString(2, record.getUsername());
            statement.setInt(3, record.getCategory());
            statement.setInt(4, record.getEvent());
            statement.setString(5, record.getMetadata());
            statement.setString(6, record.getRawData());

            statement.execute();

            statement.close();
            connection.close();
        } catch (Exception e){
            throw new ProviderException("Exception caught when pushRecord:", e);
        }
    }

}
