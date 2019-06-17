package net.easecation.playeractionrecorder.provider;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class C3p0ConnectionPool {

    private ComboPooledDataSource ds;

    private static C3p0ConnectionPool pool;

    private C3p0ConnectionPool(ComboPooledDataSource ds) {
        this.ds = ds;
    }

    public static void loadConfig(ComboPooledDataSource ds) {
        try {
            pool = new C3p0ConnectionPool(ds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //加上synchronized就是典型的同步模式
    public static /*synchronized*/ C3p0ConnectionPool getInstance() {
        if (pool == null) {
            throw new RuntimeException("Database not connected!");
        }
        return pool;
    }

    //synchronized保证每个pool线程请求返回的都是不同的Connection
    public synchronized final Connection getConnection() {
        try {
            Connection connection = ds.getConnection();
            // 1)将配置文件中的c3p0.initialPoolSize，c3p0.minPoolSize，c3p0.maxPoolSize都改为1，然后执行查看结果
            // 2)将配置文件中的c3p0.initialPoolSize，c3p0.minPoolSize，c3p0.maxPoolSize都改为较小的值，加大线程数，然后执行查看结果
            System.out.println(" **** current connection number: " + ((ComboPooledDataSource)ds).getNumConnections()
                    + " busy connection number: " + ((ComboPooledDataSource)ds).getNumBusyConnections()
                    + " Idle connection nmuber: " + ((ComboPooledDataSource)ds).getNumIdleConnections());
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void shutdown() {
        ds.close();
    }

}
