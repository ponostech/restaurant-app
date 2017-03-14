package restaurant.models;

public class InvoicePrint {

	private int slNo;
	private String name;
	private String price;
	private int qty;
	private String amount;

	public InvoicePrint(int slNo, String name, String price, int qty, String amount) {
		this.slNo = slNo;
		this.name = name;
		this.price = price;
		this.qty = qty;
		this.amount = amount;
	}

	public int getSlNo() {
		return slNo;
	}

	public void setSlNo(int slNo) {
		this.slNo = slNo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "InvoicePrint [slNo=" + slNo + ", name=" + name + ", price=" + price + ", qty=" + qty + ", amount="
				+ amount + "]";
	}

}
