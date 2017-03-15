package restaurant.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.mysql.jdbc.Connection;

public class Database {
	private String dbUrl = "";
	private String dbUser = "";
	private String dbPassword = "";

	private Connection connection = null;

	public Database() throws SQLException {
		connect();
	}

	private void connect() throws SQLException {
		Properties properties = new Properties();
		try {
			properties
					.load(new FileInputStream(System.getProperty("user.dir") + File.separator + "database.properties"));
			dbUrl = properties.getProperty("url");
			dbUser = properties.getProperty("username");
			dbPassword = properties.getProperty("password");
			connection = (Connection) DriverManager.getConnection(dbUrl, dbUser, dbPassword);

		} catch (FileNotFoundException e) {
			properties.setProperty("url", "jdbc:mysql://localhost:3306/ponos_restaurant");
			properties.setProperty("username", "root");
			properties.setProperty("password", "");
			try {
				properties.store(
						new FileOutputStream(System.getProperty("user.dir") + File.separator + "database.properties"),
						"Database");
				connection = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/restaurant_app",
						"root", "");
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection() throws SQLException {
		return connection;
	}

	public void closeConnection() throws SQLException {
		connection.close();
	}
	
}
