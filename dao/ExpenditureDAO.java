package restaurant.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

import restaurant.helpers.Message;
import restaurant.models.Expenditure;

public class ExpenditureDAO {

	public Expenditure save(Expenditure exp) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement(
					"insert into expenses (user_id,description,amount,datetime) values(?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, exp.getUserId());
			stmt.setString(2, exp.getDescription());
			stmt.setFloat(3, exp.getAmount());
			stmt.setString(4, exp.getDatetime());
			stmt.executeUpdate();
			ResultSet resultSet = stmt.getGeneratedKeys();

			if (resultSet.next()) {
				exp = get(resultSet.getLong(1));
			} else {
				exp.setErrorMessage(Message.ERROR);
			}

		} catch (SQLException e) {
			exp.setErrorMessage("Error: " + e.getErrorCode() + " - " + e.getLocalizedMessage());
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return exp;
	}

	public Expenditure update(Expenditure exp) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("update expenses set user_id=?,description=?,amount=?,datetime=?  where id=?");
			stmt.setInt(1, exp.getUserId());
			stmt.setString(2, exp.getDescription());
			stmt.setFloat(3, exp.getAmount());
			stmt.setString(4, exp.getDatetime());
			stmt.setLong(5, exp.getId());
			if (stmt.executeUpdate() == 0) {
				exp.setErrorMessage(Message.ERROR);
			} else {
				return get(exp.getId());
			}

		} catch (SQLException e) {
			exp.setErrorMessage("Error: " + e.getErrorCode() + " - " + e.getLocalizedMessage());
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return exp;
	}

	public ArrayList<Expenditure> search(String keyword) {
		ArrayList<Expenditure> expenses = new ArrayList<>();
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select * from expenses where description  like ?");
			stmt.setString(1, "%" + keyword + "%");
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				expenses.add(new Expenditure(resultSet.getLong(1), resultSet.getInt(2), resultSet.getString(3),
						resultSet.getFloat(4), resultSet.getString(5)));
			}
			return expenses;

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

	public boolean delete(Long expId) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("delete from expenses where id=?");
			stmt.setLong(1, expId);
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

	public Expenditure get(Long expId) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select * from expenses where id=?");
			stmt.setLong(1, expId);
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				return new Expenditure(resultSet.getLong(1), resultSet.getInt(2), resultSet.getString(3),
						resultSet.getFloat(4), resultSet.getString(5));
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

	public ArrayList<Expenditure> getExpenditures() {
		ArrayList<Expenditure> expenses = new ArrayList<>();
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select * from expenses order by datetime desc");
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				expenses.add(new Expenditure(resultSet.getLong(1), resultSet.getInt(2), resultSet.getString(3),
						resultSet.getFloat(4), resultSet.getString(5)));
			}
			return expenses;

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

	public ArrayList<Expenditure> getExpenditures(String fromDate, String toDate) {
		ArrayList<Expenditure> items = new ArrayList<>();
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement(
					"select * from expenses" + " where datetime >= ? and datetime <= ? order by datetime desc");
			stmt.setString(1, fromDate);
			stmt.setString(2, toDate);

			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				items.add(new Expenditure(resultSet.getLong(1), resultSet.getInt(2), resultSet.getString(3),
						resultSet.getFloat(4), resultSet.getString(5)));
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
}
