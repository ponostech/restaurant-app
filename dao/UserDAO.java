package restaurant.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

import restaurant.helpers.Message;
import restaurant.models.User;

public class UserDAO {

	public User save(User user) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement(
					"insert into users (username,password,firstname,lastname,email,phone_no,address,role) values(?,?,?,?,?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, user.getUsername());
			stmt.setString(2, user.getPassword());
			stmt.setString(3, user.getFirstName());
			stmt.setString(4, user.getLastName());
			stmt.setString(5, user.getEmail());
			stmt.setString(6, user.getPhoneNo());
			stmt.setString(7, user.getAddress());
			stmt.setInt(8, user.getRole());
			stmt.executeUpdate();
			ResultSet resultSet = stmt.getGeneratedKeys();

			if (resultSet.next()) {
				user.setUserId(resultSet.getInt(1));
			} else {
				user.setErrorMessage(Message.ERROR);
			}

		} catch (SQLException e) {
			if (e.getErrorCode() == 1062) {
				user.setErrorMessage("User '" + user.getUsername() + "' already exist.");
			} else {
				user.setErrorMessage("Error: " + e.getErrorCode() + " - " + e.getLocalizedMessage());
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
		return user;
	}

	public User update(User user) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("update users set username=?,password=?,firstname=?,lastname=?,email=?,"
							+ "phone_no=?,address=?,role=? where id=?");
			stmt.setString(1, user.getUsername());
			stmt.setString(2, user.getPassword());
			stmt.setString(3, user.getFirstName());
			stmt.setString(4, user.getLastName());
			stmt.setString(5, user.getEmail());
			stmt.setString(6, user.getPhoneNo());
			stmt.setString(7, user.getAddress());
			stmt.setInt(8, user.getRole());
			stmt.setInt(9, user.getUserId());
			if (stmt.executeUpdate() == 0) {
				user.setErrorMessage(Message.ERROR);
			}

		} catch (SQLException e) {
			user.setErrorMessage("Error: " + e.getErrorCode() + " - " + e.getLocalizedMessage());
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return user;
	}

	public ArrayList<User> search(String keyword) {
		ArrayList<User> users = new ArrayList<>();
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement(
					"select * from users where username like ? or firstname  like ? or lastname  like ?");
			stmt.setString(1, "%" + keyword + "%");
			stmt.setString(2, "%" + keyword + "%");
			stmt.setString(3, "%" + keyword + "%");
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				users.add(new User(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3),
						resultSet.getString(4), resultSet.getString(5), resultSet.getString(6), resultSet.getString(7),
						resultSet.getString(8), resultSet.getInt(9)));
			}
			return users;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean delete(int userId) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("delete from users where id=?");
			stmt.setInt(1, userId);
			if (stmt.executeUpdate() != 0) {
				return true;
			}
		} catch (SQLException e) {
			return false;
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

	public User get(Integer userId) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select * from users where id=?");
			stmt.setInt(1, userId);
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				return new User(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3),
						resultSet.getString(4), resultSet.getString(5), resultSet.getString(6), resultSet.getString(7),
						resultSet.getString(8), resultSet.getInt(9));
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public User get(String username, String password) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select * from users where username=? and password=?");
			stmt.setString(1, username);
			stmt.setString(2, password);
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				return new User(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3),
						resultSet.getString(4), resultSet.getString(5), resultSet.getString(6), resultSet.getString(7),
						resultSet.getString(8), resultSet.getInt(9));
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public ArrayList<User> getUsers() {
		ArrayList<User> users = new ArrayList<>();
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select * from users");
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				users.add(new User(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3),
						resultSet.getString(4), resultSet.getString(5), resultSet.getString(6), resultSet.getString(7),
						resultSet.getString(8), resultSet.getInt(9)));
			}
			return users;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
