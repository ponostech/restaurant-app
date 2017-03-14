package restaurant.models;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class InvoiceItem {

	private LongProperty id;
	private IntegerProperty itemId;
	private LongProperty invoiceId;
	private FloatProperty price;
	private IntegerProperty quantity;
	private FloatProperty total;
	private StringProperty datetime;
	private StringProperty errorMessage = new SimpleStringProperty();

	public InvoiceItem(Long id, Integer itemId, Long invoiceId, Float price, Integer quantity, Float total,
			String datetime) {
		this.id = new SimpleLongProperty(id);
		this.itemId = new SimpleIntegerProperty(itemId);
		this.invoiceId = new SimpleLongProperty(invoiceId);
		this.price = new SimpleFloatProperty(price);
		this.quantity = new SimpleIntegerProperty(quantity);
		this.total = new SimpleFloatProperty(total);
		this.datetime = new SimpleStringProperty(datetime);
	}

	public InvoiceItem(Long id, Integer itemId, Long invoiceId, Float price, Integer quantity, String datetime) {
		this.id = new SimpleLongProperty(id);
		this.itemId = new SimpleIntegerProperty(itemId);
		this.invoiceId = new SimpleLongProperty(invoiceId);
		this.price = new SimpleFloatProperty(price);
		this.quantity = new SimpleIntegerProperty(quantity);
		this.datetime = new SimpleStringProperty(datetime);
	}

	public LongProperty idProperty() {
		return this.id;
	}

	public long getId() {
		return this.idProperty().get();
	}

	public void setId(final long id) {
		this.idProperty().set(id);
	}

	public IntegerProperty itemIdProperty() {
		return this.itemId;
	}

	public int getItemId() {
		return this.itemIdProperty().get();
	}

	public void setItemId(final int itemId) {
		this.itemIdProperty().set(itemId);
	}

	public LongProperty invoiceIdProperty() {
		return this.invoiceId;
	}

	public long getInvoiceId() {
		return this.invoiceIdProperty().get();
	}

	public void setInvoiceId(final long invoiceId) {
		this.invoiceIdProperty().set(invoiceId);
	}

	public FloatProperty priceProperty() {
		return this.price;
	}

	public float getPrice() {
		return this.priceProperty().get();
	}

	public void setPrice(final float price) {
		this.priceProperty().set(price);
	}

	public StringProperty datetimeProperty() {
		return this.datetime;
	}

	public java.lang.String getDatetime() {
		return this.datetimeProperty().get();
	}

	public void setDatetime(final java.lang.String datetime) {
		this.datetimeProperty().set(datetime);
	}

	public StringProperty errorMessageProperty() {
		return this.errorMessage;
	}

	public java.lang.String getErrorMessage() {
		return this.errorMessageProperty().get();
	}

	public void setErrorMessage(final java.lang.String errorMessage) {
		this.errorMessageProperty().set(errorMessage);
	}

	public IntegerProperty quantityProperty() {
		return this.quantity;
	}

	public int getQuantity() {
		return this.quantityProperty().get();
	}

	public void setQuantity(final int quantity) {
		this.quantityProperty().set(quantity);
	}

	public FloatProperty totalProperty() {
		return this.total;
	}

	public float getTotal() {
		return this.totalProperty().get();
	}

	public void setTotal(final float total) {
		this.totalProperty().set(total);
	}

	@Override
	public String toString() {
		return "InvoiceItem [id=" + id + ", itemId=" + itemId + ", invoiceId=" + invoiceId + ", price=" + price
				+ ", quantity=" + quantity + ", total=" + total + ", datetime=" + datetime + ", errorMessage="
				+ errorMessage + "]";
	}

}
