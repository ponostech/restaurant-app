package restaurant.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.mysql.jdbc.Connection;

public class SettingDAO {

	public boolean save(Map<String, String> settings) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			conn.setAutoCommit(false);

			PreparedStatement stmtUpdate = conn.prepareStatement("update settings set value=? where name=?");
			PreparedStatement stmtInsert = conn.prepareStatement("insert into settings (name,value) values(?,?)");

			for (Map.Entry<String, String> setting : settings.entrySet()) {
				if(settingExist(setting.getKey())) {
					stmtUpdate.setString(1, setting.getValue());
					stmtUpdate.setString(2, setting.getKey());
					stmtUpdate.addBatch();
				}else {
					stmtInsert.setString(1, setting.getKey());
					stmtInsert.setString(2, setting.getValue());
					stmtInsert.addBatch();
				}
				
			}

			stmtUpdate.executeBatch();
			stmtInsert.executeBatch();
			conn.commit();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}

		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public String get(String name) {
		String value = null;
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select value from settings where name=?");
			stmt.setString(1, name);
			ResultSet resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				value = resultSet.getString(1);
			}

		} catch (SQLException e) {
			e.printStackTrace();

		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return value;
	}
	
	public boolean settingExist(String name) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select * from settings where name=?");
			stmt.setString(1, name);
			ResultSet resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				return true;
			}

		} catch (SQLException e) {
			e.printStackTrace();

		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return false;
	}

	public Map<String, String> getSettings() {
		Map<String, String> settings = new HashMap<>();
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select name,value from settings");
			ResultSet resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				settings.put(resultSet.getString(1), resultSet.getString(2));
			}

		} catch (SQLException e) {
			e.printStackTrace();

		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return settings;
	}

}
