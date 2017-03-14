package restaurant.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

import restaurant.helpers.Message;
import restaurant.models.Category;

public class CategoryDAO {
	public Category save(Category category) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("insert into categories (name,description) values(?,?)",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, category.getName());
			stmt.setString(2, category.getDescription());
			stmt.executeUpdate();
			ResultSet resultSet = stmt.getGeneratedKeys();

			if (resultSet.next()) {
				category = get(resultSet.getInt(1));
			} else {
				category.setErrorMessage(Message.ERROR);
			}

		} catch (SQLException e) {
			category.setErrorMessage("Error: " + e.getErrorCode() + " - " + e.getLocalizedMessage());
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return category;
	}

	public Category update(Category category) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("update categories set name=?,description=?  where id=?");
			stmt.setString(1, category.getName());
			stmt.setString(2, category.getDescription());
			stmt.setInt(3, category.getId());
			if (stmt.executeUpdate() == 0) {
				category.setErrorMessage(Message.ERROR);
			} else {
				return get(category.getId());
			}

		} catch (SQLException e) {
			category.setErrorMessage("Error: " + e.getErrorCode() + " - " + e.getLocalizedMessage());
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return category;
	}

	public ArrayList<Category> search(String keyword) {
		ArrayList<Category> categories = new ArrayList<>();
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select * from categories where name like ? or description like ?");
			stmt.setString(1, "%" + keyword + "%");
			stmt.setString(2, "%" + keyword + "%");
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				categories.add(new Category(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3)));
			}
			return categories;

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

	public boolean delete(Integer catId) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("delete from categories where id=?");
			stmt.setLong(1, catId);
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

	public Category get(Integer catId) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select * from categories where id=?");
			stmt.setInt(1, catId);
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				return new Category(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3));
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

	public ArrayList<Category> getCategories() {
		ArrayList<Category> categories = new ArrayList<>();
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select * from categories");
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				categories.add(new Category(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3)));
			}
			return categories;

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
