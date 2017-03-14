package restaurant.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

import restaurant.helpers.Message;
import restaurant.models.Item;

public class ItemDAO {

	public Item save(Item item) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement(
					"insert into items (user_id,name,price,shortcut_key,category_id) values(?,?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, item.getUserId());
			stmt.setString(2, item.getName());
			stmt.setFloat(3, item.getPrice());
			stmt.setInt(4, item.getShortCutKey());
			stmt.setInt(5, item.getCategoryId());
			stmt.executeUpdate();
			ResultSet resultSet = stmt.getGeneratedKeys();

			if (resultSet.next()) {
				item.setId(resultSet.getInt(1));
			} else {
				item.setErrorMessage(Message.ERROR);
			}

		} catch (SQLException e) {
			item.setErrorMessage("Error: " + e.getErrorCode() + " - " + e.getLocalizedMessage());
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return item;
	}

	public Item update(Item item) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement(
					"update items set user_id=?,name=?,price=?,shortcut_key=?,category_id=? where id=?");
			stmt.setInt(1, item.getUserId());
			stmt.setString(2, item.getName());
			stmt.setFloat(3, item.getPrice());
			stmt.setInt(4, item.getShortCutKey());
			stmt.setInt(5, item.getCategoryId());
			stmt.setInt(6, item.getId());
			if (stmt.executeUpdate() == 0) {
				item.setErrorMessage(Message.ERROR);
			}

		} catch (SQLException e) {
			item.setErrorMessage("Error: " + e.getErrorCode() + " - " + e.getLocalizedMessage());
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return item;
	}

	public ArrayList<Item> search(String keyword) {
		ArrayList<Item> items = new ArrayList<>();
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select * from items where name like ?");
			stmt.setString(1, "%" + keyword + "%");
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				items.add(new Item(resultSet.getInt(1), resultSet.getInt(2), resultSet.getString(3),
						resultSet.getFloat(4), resultSet.getInt(5), resultSet.getInt(6)));
			}
			return items;

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
	
	public ArrayList<Item> searchByShortKey(int key) {
		ArrayList<Item> items = new ArrayList<>();
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select * from items where shortcut_key=?");
			stmt.setInt(1,key);
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				items.add(new Item(resultSet.getInt(1), resultSet.getInt(2), resultSet.getString(3),
						resultSet.getFloat(4), resultSet.getInt(5), resultSet.getInt(6)));
			}
			return items;

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
	
	public ArrayList<Item> searchByCategory(int catId) {
		ArrayList<Item> items = new ArrayList<>();
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select * from items where category_id=?");
			stmt.setInt(1, catId);
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				items.add(new Item(resultSet.getInt(1), resultSet.getInt(2), resultSet.getString(3),
						resultSet.getFloat(4), resultSet.getInt(5), resultSet.getInt(6)));
			}
			return items;

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

	public boolean delete(int itemId) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("delete from items where id=?");
			stmt.setInt(1, itemId);
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

	public Item get(Integer itemId) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select * from items where id=?");
			stmt.setInt(1, itemId);
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				return new Item(resultSet.getInt(1), resultSet.getInt(2), resultSet.getString(3), resultSet.getFloat(4),
						resultSet.getInt(5), resultSet.getInt(6));
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

	public ArrayList<Item> getItems() {
		ArrayList<Item> items = new ArrayList<>();
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select * from items");
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				items.add(new Item(resultSet.getInt(1), resultSet.getInt(2), resultSet.getString(3),
						resultSet.getFloat(4), resultSet.getInt(5), resultSet.getInt(6)));
			}
			return items;

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
	
	public int countItems() {
		int size = 0;
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select count(*) from items");
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				size = resultSet.getInt(1);
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
		return size;
	}

}
