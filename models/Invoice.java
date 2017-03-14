package restaurant.models;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Invoice {

	private LongProperty id;
	private IntegerProperty userId;
	private LongProperty customerId;
	private FloatProperty tax;
	private FloatProperty serviceCharge;
	private FloatProperty packingChargeAmount;
	private FloatProperty discount;
	private FloatProperty total;
	private StringProperty tableName;
	private StringProperty datetime;
	private StringProperty errorMessage = new SimpleStringProperty();

	public Invoice(Long id, Integer userId, Long customerId, Float tax, Float serviceCharge, Float packingChargeAmount,
			Float discount, Float total, String tableName, String datetime) {
		this.id = new SimpleLongProperty(id);
		this.userId = new SimpleIntegerProperty(userId);
		this.customerId = new SimpleLongProperty(customerId);
		this.tableName = new SimpleStringProperty(tableName);
		this.total = new SimpleFloatProperty(total);
		this.tax = new SimpleFloatProperty(tax);
		this.serviceCharge = new SimpleFloatProperty(serviceCharge);
		this.packingChargeAmount = new SimpleFloatProperty(packingChargeAmount);
		this.discount = new SimpleFloatProperty(discount);
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

	public IntegerProperty userIdProperty() {
		return this.userId;
	}

	public int getUserId() {
		return this.userIdProperty().get();
	}

	public void setUserId(final int userId) {
		this.userIdProperty().set(userId);
	}

	public LongProperty customerIdProperty() {
		return this.customerId;
	}

	public long getCustomerId() {
		return this.customerIdProperty().get();
	}

	public void setCustomerId(final long customerId) {
		this.customerIdProperty().set(customerId);
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

	public StringProperty datetimeProperty() {
		return this.datetime;
	}

	public String getDatetime() {
		return this.datetimeProperty().get();
	}

	public void setDatetime(final String datetime) {
		this.datetimeProperty().set(datetime);
	}

	public StringProperty errorMessageProperty() {
		return this.errorMessage;
	}

	public String getErrorMessage() {
		return this.errorMessageProperty().get();
	}

	public void setErrorMessage(final String errorMessage) {
		this.errorMessageProperty().set(errorMessage);
	}

	public StringProperty tableNameProperty() {
		return this.tableName;
	}

	public java.lang.String getTableName() {
		return this.tableNameProperty().get();
	}

	public void setTableName(final java.lang.String tableName) {
		this.tableNameProperty().set(tableName);
	}

	public FloatProperty packingChargeAmountProperty() {
		return this.packingChargeAmount;
	}

	public float getPackingChargeAmount() {
		return this.packingChargeAmountProperty().get();
	}

	public void setPackingChargeAmount(final float packingChargeAmount) {
		this.packingChargeAmountProperty().set(packingChargeAmount);
	}

	public FloatProperty taxProperty() {
		return this.tax;
	}

	public float getTax() {
		return this.taxProperty().get();
	}

	public void setTax(final float tax) {
		this.taxProperty().set(tax);
	}

	public FloatProperty serviceChargeProperty() {
		return this.serviceCharge;
	}

	public float getServiceCharge() {
		return this.serviceChargeProperty().get();
	}

	public void setServiceCharge(final float serviceCharge) {
		this.serviceChargeProperty().set(serviceCharge);
	}

	public FloatProperty discountProperty() {
		return this.discount;
	}

	public float getDiscount() {
		return this.discountProperty().get();
	}

	public void setDiscount(final float discount) {
		this.discountProperty().set(discount);
	}

	@Override
	public String toString() {
		return "Invoice [id=" + id + ", userId=" + userId + ", customerId=" + customerId + ", tax=" + tax
				+ ", serviceCharge=" + serviceCharge + ", packingChargeAmount=" + packingChargeAmount + ", discount="
				+ discount + ", total=" + total + ", tableName=" + tableName + ", datetime=" + datetime
				+ ", errorMessage=" + errorMessage + "]";
	}

}
