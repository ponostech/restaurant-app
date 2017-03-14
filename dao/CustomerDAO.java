package restaurant.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

import restaurant.helpers.Message;
import restaurant.models.Customer;

public class CustomerDAO {

	public Customer save(Customer customer) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement(
					"insert into customers (firstname,lastname,email,phone_no,address) values(?,?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, customer.getFirstName());
			stmt.setString(2, customer.getLastName());
			stmt.setString(3, customer.getEmail());
			stmt.setString(4, customer.getPhoneNo());
			stmt.setString(5, customer.getAddress());
			stmt.executeUpdate();
			ResultSet resultSet = stmt.getGeneratedKeys();

			if (resultSet.next()) {
				customer.setId(resultSet.getInt(1));
			} else {
				customer.setErrorMessage(Message.ERROR);
			}

		} catch (SQLException e) {
			customer.setErrorMessage("Error: " + e.getErrorCode() + " - " + e.getLocalizedMessage());
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return customer;
	}

	public Customer update(Customer customer) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement(
					"update customers set firstname=?,lastname=?,email=?,phone_no=?,address=? where id=?");
			stmt.setString(1, customer.getFirstName());
			stmt.setString(2, customer.getLastName());
			stmt.setString(3, customer.getEmail());
			stmt.setString(4, customer.getPhoneNo());
			stmt.setString(5, customer.getAddress());
			stmt.setLong(6, customer.getId());
			if (stmt.executeUpdate() == 0) {
				customer.setErrorMessage(Message.ERROR);
			}

		} catch (SQLException e) {
			customer.setErrorMessage("Error: " + e.getErrorCode() + " - " + e.getLocalizedMessage());
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return customer;
	}

	public ArrayList<Customer> search(String keyword) {
		ArrayList<Customer> customers = new ArrayList<>();
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement(
					"select * from customers where firstname  like ? or lastname  like ? or address like ?");
			stmt.setString(1, "%" + keyword + "%");
			stmt.setString(2, "%" + keyword + "%");
			stmt.setString(3, "%" + keyword + "%");
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				customers.add(new Customer(resultSet.getLong(1), resultSet.getString(2), resultSet.getString(3),
						resultSet.getString(4), resultSet.getString(5), resultSet.getString(6)));
			}
			return customers;

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

	public boolean delete(Long customerId) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("delete from customers where id=?");
			stmt.setLong(1, customerId);
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

	public Customer get(Long customerId) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select * from customers where id=?");
			stmt.setLong(1, customerId);
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				return new Customer(resultSet.getLong(1), resultSet.getString(2), resultSet.getString(3),
						resultSet.getString(4), resultSet.getString(5), resultSet.getString(6));
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

	public ArrayList<Customer> getCustomers() {
		ArrayList<Customer> customers = new ArrayList<>();
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select * from customers");
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				customers.add(new Customer(resultSet.getLong(1), resultSet.getString(2), resultSet.getString(3),
						resultSet.getString(4), resultSet.getString(5), resultSet.getString(6)));
			}
			return customers;

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
