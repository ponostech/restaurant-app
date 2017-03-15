package restaurant.models;

public class SaleReport {

	private long invoiceNo;
	private String date;
	private String customer;
	private String table;
	private String amount;

	public SaleReport(long invoiceNo, String date, String customer, String table, String amount) {
		this.invoiceNo = invoiceNo;
		this.date = date;
		this.customer = customer;
		this.table = table;
		this.amount = amount;
	}

	public long getInvoiceNo() {
		return invoiceNo;
	}

	public void setInvoiceNo(long invoiceNo) {
		this.invoiceNo = invoiceNo;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getCustomer() {
		return customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "SaleReport [invoiceNo=" + invoiceNo + ", date=" + date + ", customer=" + customer + ", table=" + table
				+ ", amount=" + amount + "]";
	}

}
