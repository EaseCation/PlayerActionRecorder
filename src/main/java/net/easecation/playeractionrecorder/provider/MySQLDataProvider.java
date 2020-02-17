package net.easecation.playeractionrecorder.provider;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import net.easecation.playeractionrecorder.action.ActionDataEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

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

    public void pushRecords(ActionDataEntry[] records) throws ProviderException {
        try {
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO recorder.records (logtime, username, category, event, metadata, rawdata) VALUES (?, ?, ?, ?, ?, ?)"
            );
            for (ActionDataEntry record : records) {
                statement.setTimestamp(1, new Timestamp(record.getLogTime()));
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
