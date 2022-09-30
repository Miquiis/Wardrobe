package me.miquiis.wardrobe.server.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class MySQL {

	// instance variables
	private String host;
	private String port;
	private String database;
	private String username;
	private String password;

	private Connection connection;

	/**
	 * creates a mysql object (does not yet establish a connection)
	 * 
	 * @param host     the hostname
	 * @param port     the port of the sql server
	 * @param database the name of the database
	 * @param username the username
	 * @param password the corresponding password
	 */
	public MySQL(String host, String port, String database, String username, String password) {
		this.host = host;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
		this.connection = null;
	}

	/**
	 * establishes the connection to the sql server
	 */
	public boolean connect() {
		try {
			connection = DriverManager.getConnection(
					"jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true", username, password);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * closes existing sql connection
	 */
	public void disconnect() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the sql connection
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * executes an update
	 * 
	 * @param query the statement for the update
	 */
	public CompletableFuture<Void> asyncUpdate(String query)
	{
		return CompletableFuture.supplyAsync(() -> {
			try {
				if (connection.isClosed())
					connect();
				connection.createStatement().executeUpdate(query);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null;
		});
	}

	public CompletableFuture<Void> asyncMultipleUpdate(ArrayList<String> queries)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			try {
				if (connection.isClosed())
					connect();

				Statement st = connection.createStatement();

				for (String query : queries)
				{
					st.addBatch(query);
				}

				st.executeBatch();

			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null;
		});
	}

	public CompletableFuture<ResultSet> asyncResult(String query)
	{
		return CompletableFuture.supplyAsync(() -> {
			try {
				if (connection.isClosed())
					connect();
				return connection.createStatement().executeQuery(query);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null;
		});
	}

	public void update(String query) {
		try {
			if (connection.isClosed())
				connect();
			connection.createStatement().executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param query the statement for the result
	 * @return the corresponding result
	 */
	public ResultSet getResult(String query) {
		try {
			if (connection.isClosed())
				connect();
			return connection.createStatement().executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}