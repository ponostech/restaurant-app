package restaurant.models;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Expenditure {
	private LongProperty id;
	private IntegerProperty userId;
	private StringProperty description;
	private FloatProperty amount;
	private StringProperty datetime;
	private StringProperty errorMessage = new SimpleStringProperty();

	public Expenditure(Long id, Integer userId, String description, Float amount, String datetime) {
		this.id = new SimpleLongProperty(id);
		this.userId = new SimpleIntegerProperty(userId);
		this.description = new SimpleStringProperty(description);
		this.amount = new SimpleFloatProperty(amount);
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

	public StringProperty descriptionProperty() {
		return this.description;
	}

	public String getDescription() {
		return this.descriptionProperty().get();
	}

	public void setDescription(final String description) {
		this.descriptionProperty().set(description);
	}

	public FloatProperty amountProperty() {
		return this.amount;
	}

	public float getAmount() {
		return this.amountProperty().get();
	}

	public void setAmount(final float amount) {
		this.amountProperty().set(amount);
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

	@Override
	public String toString() {
		return "Expenditure [id=" + id + ", userId=" + userId + ", description=" + description + ", amount=" + amount
				+ ", datetime=" + datetime + ", errorMessage=" + errorMessage + "]";
	}

}
