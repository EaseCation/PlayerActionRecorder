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

    public void pushRecord(ActionDataEntry record) throws ProviderException {
        try {
            Connection connection = getConnection();
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO recorder.records (logtime, username, category, event, metadata, rawdata) VALUES (?, ?, ?, ?, ?, ?)"
            );
            statement.setTimestamp(1, new Timestamp(record.getLogtime()));
            statement.setString(2, record.getUsername());
            statement.setString(3, record.getCategory().name());
            statement.setString(4, record.getEvent().name());
            statement.setString(5, record.getMetadata());
            statement.setString(6, record.getRawdata());

            statement.execute();

            statement.close();
            connection.close();
        } catch (Exception e){
            throw new ProviderException("Exception caught when pushRecord:", e);
        }
    }

}
