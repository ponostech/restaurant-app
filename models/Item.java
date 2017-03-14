package restaurant.models;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Item {
	private IntegerProperty id ;
	private IntegerProperty userId;
	private StringProperty name;
	private FloatProperty price;
	private IntegerProperty shortCutKey;
	private IntegerProperty categoryId;
	private StringProperty errorMessage = new SimpleStringProperty();

	public Item(Integer id, Integer userId, String name, Float price, Integer shortCutKey, Integer categoryId) {
		this.id = new SimpleIntegerProperty(id);
		this.userId = new SimpleIntegerProperty(userId);
		this.name = new SimpleStringProperty(name);
		this.price = new SimpleFloatProperty(price);
		this.categoryId = new SimpleIntegerProperty(categoryId);
		this.shortCutKey = new SimpleIntegerProperty(shortCutKey);
	}	
	
	public IntegerProperty idProperty() {
		return this.id;
	}

	public int getId() {
		return this.idProperty().get();
	}

	public void setId(final int id) {
		this.idProperty().set(id);
	}

	public StringProperty nameProperty() {
		return this.name;
	}

	public java.lang.String getName() {
		return this.nameProperty().get();
	}

	public void setName(final java.lang.String name) {
		this.nameProperty().set(name);
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

	public IntegerProperty categoryIdProperty() {
		return this.categoryId;
	}

	public int getCategoryId() {
		return this.categoryIdProperty().get();
	}

	public void setCategoryId(final int categoryId) {
		this.categoryIdProperty().set(categoryId);
	}

	public IntegerProperty shortCutKeyProperty() {
		return this.shortCutKey;
	}

	public int getShortCutKey() {
		return this.shortCutKeyProperty().get();
	}

	public void setShortCutKey(final int shortCutKey) {
		this.shortCutKeyProperty().set(shortCutKey);
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

	public IntegerProperty userIdProperty() {
		return this.userId;
	}

	public int getUserId() {
		return this.userIdProperty().get();
	}

	public void setUserId(final int userId) {
		this.userIdProperty().set(userId);
	}

	@Override
	public String toString() {
		return "Item [id=" + id + ", userId=" + userId + ", name=" + name + ", price=" + price + ", categoryId="
				+ categoryId + ", shortCutKey=" + shortCutKey + ", errorMessage=" + errorMessage + "]";
	}

}
