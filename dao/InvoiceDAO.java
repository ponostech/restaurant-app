package restaurant.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

import javafx.collections.ObservableList;
import restaurant.helpers.Message;
import restaurant.models.Invoice;
import restaurant.models.InvoiceItem;

public class InvoiceDAO {

	public Invoice save(Invoice invoice, ObservableList<InvoiceItem> invoiceItems) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			conn.setAutoCommit(false);

			PreparedStatement stmt = conn.prepareStatement(
					"insert into invoices (user_id,customer_id,tax,service_charge,packing_charge,discount, total,table_name,datetime) "
							+ "values(?,?,?,?,?,?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			stmt.setInt(1, invoice.getUserId());
			stmt.setLong(2, invoice.getCustomerId());
			stmt.setFloat(3, invoice.getTax());
			stmt.setFloat(4, invoice.getServiceCharge());
			stmt.setFloat(5, invoice.getPackingChargeAmount());
			stmt.setFloat(6, invoice.getDiscount());
			stmt.setFloat(7, invoice.getTotal());
			stmt.setString(8, invoice.getTableName());
			stmt.setString(9, invoice.getDatetime());

			stmt.executeUpdate();
			ResultSet resultSet = stmt.getGeneratedKeys();
			if (resultSet.next()) {
				invoice.setId(resultSet.getLong(1));
				PreparedStatement stmtItems = conn.prepareStatement(
						"insert into invoice_items (item_id,invoice_id,price,qty,datetime) values(?,?,?,?,?)");
				invoiceItems.forEach(item -> {
					try {
						item.setDatetime(invoice.getDatetime());
						stmtItems.setInt(1, item.getItemId());
						stmtItems.setLong(2, invoice.getId());
						stmtItems.setFloat(3, item.getPrice());
						stmtItems.setInt(4, item.getQuantity());
						stmtItems.setString(5, item.getDatetime());
						stmtItems.addBatch();
					} catch (Exception e) {
						invoice.setErrorMessage(e.getMessage());
						e.printStackTrace();
					}
				});

				stmtItems.executeBatch();
				conn.commit();

			} else {
				invoice.setErrorMessage(Message.ERROR);

				try {
					conn.rollback();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
			invoice.setErrorMessage(e.getMessage());
			try {
				conn.rollback();
			} catch (SQLException e1) {
				invoice.setErrorMessage(e1.getMessage());
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
		return invoice;
	}

	public Invoice update(Invoice invoice, ObservableList<InvoiceItem> invoiceItems) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			conn.setAutoCommit(false);

			PreparedStatement stmt = conn
					.prepareStatement("update invoices set user_id=?,customer_id=?,tax=?,service_charge=?,"
							+ "packing_charge=?,discount=?,total=?,table_name=?,datetime=? where id=?");
			stmt.setInt(1, invoice.getUserId());
			stmt.setLong(2, invoice.getCustomerId());
			stmt.setFloat(3, invoice.getTax());
			stmt.setFloat(4, invoice.getServiceCharge());
			stmt.setFloat(5, invoice.getPackingChargeAmount());
			stmt.setFloat(6, invoice.getDiscount());
			stmt.setFloat(7, invoice.getTotal());
			stmt.setString(8, invoice.getTableName());
			stmt.setString(9, invoice.getDatetime());
			stmt.setLong(10, invoice.getId());

			if (stmt.executeUpdate() != 0) {
				PreparedStatement stmtItems = conn.prepareStatement(
						"insert into invoice_items (item_id,invoice_id,price,qty,datetime) values(?,?,?,?,?)");
				if (deleteInvoiceItems(invoice.getId())) {

					invoiceItems.forEach(item -> {
						try {
							item.setDatetime(invoice.getDatetime());
							stmtItems.setInt(1, item.getItemId());
							stmtItems.setLong(2, invoice.getId());
							stmtItems.setFloat(3, item.getPrice());
							stmtItems.setInt(4, item.getQuantity());
							stmtItems.setString(5, item.getDatetime());
							stmtItems.addBatch();
						} catch (Exception e) {
							invoice.setErrorMessage(e.getMessage());
							e.printStackTrace();
						}
					});

					stmtItems.executeBatch();
					conn.commit();
				} else {
					invoice.setErrorMessage(Message.ERROR);
					try {
						conn.rollback();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				invoice.setErrorMessage(Message.ERROR);

				try {
					conn.rollback();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
			invoice.setErrorMessage(e.getMessage());
			try {
				conn.rollback();
			} catch (SQLException e1) {
				invoice.setErrorMessage(e1.getMessage());
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
		return get(invoice.getId());
	}

	public ArrayList<Invoice> searchByTableName(String tableName) {
		ArrayList<Invoice> items = new ArrayList<>();
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select * from invoices where table_name like ?");
			if (tableName.equals("All Tables")) {
				stmt.setString(1, "%A%"); // get all tables
			} else {
				stmt.setString(1, "%" + tableName + "%");
			}
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				items.add(new Invoice(resultSet.getLong(1), resultSet.getInt(2), resultSet.getLong(3),
						resultSet.getFloat(4), resultSet.getFloat(5), resultSet.getFloat(6), resultSet.getFloat(7),
						resultSet.getFloat(8), resultSet.getString(9), resultSet.getString(10)));
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

	public boolean delete(long invoiceId) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();

			PreparedStatement stmt = conn.prepareStatement("delete from invoices where id=?");
			stmt.setLong(1, invoiceId);

			if (stmt.executeUpdate() != 0) {
				deleteInvoiceItems(invoiceId);
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

	public boolean deleteInvoiceItems(long invoiceId) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();

			PreparedStatement stmt = conn.prepareStatement("delete from invoice_items where invoice_id=?");
			stmt.setLong(1, invoiceId);

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

	public Invoice get(long invoiceId) {
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select * from invoices where id=?");
			stmt.setLong(1, invoiceId);
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				return new Invoice(resultSet.getLong(1), resultSet.getInt(2), resultSet.getLong(3),
						resultSet.getFloat(4), resultSet.getFloat(5), resultSet.getFloat(6), resultSet.getFloat(7),
						resultSet.getFloat(8), resultSet.getString(9), resultSet.getString(10));
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

	public ArrayList<Invoice> getInvoices() {
		ArrayList<Invoice> items = new ArrayList<>();
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select * from invoices order by datetime desc");
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				items.add(new Invoice(resultSet.getLong(1), resultSet.getInt(2), resultSet.getLong(3),
						resultSet.getFloat(4), resultSet.getFloat(5), resultSet.getFloat(6), resultSet.getFloat(7),
						resultSet.getFloat(8), resultSet.getString(9), resultSet.getString(10)));
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

	public ArrayList<Invoice> getInvoices(String fromDate, String toDate) { 
		ArrayList<Invoice> items = new ArrayList<>();
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = null;

			stmt = conn.prepareStatement(
					"select * from invoices" + " where datetime >= ? and datetime <= ? order by datetime desc");
			stmt.setString(1, fromDate);
			stmt.setString(2, toDate);

			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				items.add(new Invoice(resultSet.getLong(1), resultSet.getInt(2), resultSet.getLong(3),
						resultSet.getFloat(4), resultSet.getFloat(5), resultSet.getFloat(6), resultSet.getFloat(7),
						resultSet.getFloat(8), resultSet.getString(9), resultSet.getString(10)));
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

	public ArrayList<Invoice> getCustomerInvoices(long id) {
		ArrayList<Invoice> items = new ArrayList<>();
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select * from invoices where customer_id=? order by datetime desc");
			stmt.setLong(1, id);
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				items.add(new Invoice(resultSet.getLong(1), resultSet.getInt(2), resultSet.getLong(3),
						resultSet.getFloat(4), resultSet.getFloat(5), resultSet.getFloat(6), resultSet.getFloat(7),
						resultSet.getFloat(8), resultSet.getString(9), resultSet.getString(10)));
			}
			// System.out.println(items);
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

	public ArrayList<Invoice> getCustomerInvoices(String fromDate, String toDate, long id) {
		ArrayList<Invoice> items = new ArrayList<>();
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select * from invoices"
					+ " where datetime >= ? and datetime <= ? and customer_id=? order by datetime desc");
			stmt.setString(1, fromDate);
			stmt.setString(2, toDate);
			stmt.setLong(3, id);

			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				items.add(new Invoice(resultSet.getLong(1), resultSet.getInt(2), resultSet.getLong(3),
						resultSet.getFloat(4), resultSet.getFloat(5), resultSet.getFloat(6), resultSet.getFloat(7),
						resultSet.getFloat(8), resultSet.getString(9), resultSet.getString(10)));
			}
			// System.out.println(items);
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

	public ArrayList<String> getTables() {
		ArrayList<String> tables = new ArrayList<>();
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select distinct(table_name) from invoices");
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				tables.add(resultSet.getString(1));
			}
			return tables;

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

	public ArrayList<InvoiceItem> getInvoiceItems(long invoiceId) {
		ArrayList<InvoiceItem> items = new ArrayList<>();
		Connection conn = null;
		try {
			Database dbObj = new Database();
			conn = dbObj.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select * from invoice_items where invoice_id=?");
			stmt.setLong(1, invoiceId);
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) {
				items.add(new InvoiceItem(resultSet.getLong(1), resultSet.getInt(2), resultSet.getLong(3),
						resultSet.getFloat(4), resultSet.getInt(5), resultSet.getFloat(4) * resultSet.getInt(5),
						resultSet.getString(6)));
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
