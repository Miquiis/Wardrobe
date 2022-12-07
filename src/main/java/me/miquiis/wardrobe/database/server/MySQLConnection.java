package me.miquiis.wardrobe.database.server;

import me.miquiis.wardrobe.common.Configs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

public class MySQLConnection {
    private static final Logger LOGGER = LogManager.getLogger();
    private final String host;
    private final String port;
    private final String database;
    private final String username;
    private final String password;
    private Connection connection;

    public MySQLConnection() {
        host = Configs.DATABASE_CONFIG.host.get();
        port = Configs.DATABASE_CONFIG.port.get();
        database = Configs.DATABASE_CONFIG.database.get();
        username = Configs.DATABASE_CONFIG.username.get();
        password = Configs.DATABASE_CONFIG.password.get();
    }

    public Connection getConnection() {
        return connection;
    }

    public void connect() throws Exception {
        try {
            if (getConnection() != null) {
                connection.close();
            }
        } catch (Exception ignored) {}
        connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
        } catch (Exception e) {
            LOGGER.error("There has been an error:" + e.getMessage());
            LOGGER.error("Failed to connect to the MySQL");
        }
    }

    public CompletableFuture<Void> asyncBatch(String ...queries)
    {
        return CompletableFuture.supplyAsync(() -> {
            try {
                connect();
                connection.setAutoCommit(false);
                Statement statement = connection.createStatement();
                for (String query : queries) {
                    statement.addBatch(query);
                }
                statement.executeBatch();
                connection.commit();
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public CompletableFuture<Void> asyncUpdate(String query)
    {
        return CompletableFuture.supplyAsync(() -> {
            try {
                connect();
                connection.createStatement().executeUpdate(query);
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public CompletableFuture<ResultSet> asyncResult(String query)
    {
        return CompletableFuture.supplyAsync(() -> {
            try {
                connect();
                return connection.createStatement().executeQuery(query);
            } catch (Exception e) {

            }
            return null;
        });
    }

    public ResultSet query(String query) {
        if (query == null) {
            return null;
        }
        ResultSet results = null;
        try {
            Statement statement = getConnection().createStatement();
            results = statement.executeQuery(query);
        } catch (Exception e) {
            LOGGER.error("There has been an error:" + e.getMessage());
            LOGGER.error("Failed Query in MySql using the following query input:");
            LOGGER.error(query);
        }
        return results;
    }
}
