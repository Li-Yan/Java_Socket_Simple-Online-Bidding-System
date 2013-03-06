package Database;

import java.sql.*;

public class ServerDB {
	private Connection dbConnection = null;
	private Statement dbStatement = null;
	private ResultSet dbResultSet = null;
	
	public ServerDB() throws ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		try {
			dbConnection = DriverManager.getConnection("jdbc:sqlite:server.db");
			dbStatement = dbConnection.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void ServeDB_Close() {
		try {
			dbResultSet.close();
			dbStatement.close();
			dbConnection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void DB_Reset() {
		try {
			dbStatement.executeUpdate("DROP TABLE IF EXISTS items;");
			dbStatement.executeUpdate("CREATE TABLE items (id INTEGER PRIMARY KEY, sellername VARCHAR(64), " +
					"itemname VARCHAR(64), translimit INTEGER, bidby VARCHAR(64), currentbid INTEGER, " +
					"buynow INTEGER, desc VARCHAR(1024))");
			dbStatement.executeUpdate("DROP TABLE IF EXISTS messages;");
			dbStatement.executeUpdate("CREATE TABLE messages (username VARCHAR(64), msg VARCHAR(1024));");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ResultSet sqlite_Query(String query) {
		try {
			dbResultSet = dbStatement.executeQuery(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dbResultSet;
	}
	
	public int sqlite_Execute(String query) {
		try {
			dbStatement.executeUpdate(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 1;
	}
}
