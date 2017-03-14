package restaurant.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Category {

	private IntegerProperty id;
	private StringProperty name;
	private StringProperty description;
	private StringProperty errorMessage = new SimpleStringProperty();

	public Category(Integer id, String name, String description) {
		this.id = new SimpleIntegerProperty(id);
		this.name = new SimpleStringProperty(name);
		this.description = new SimpleStringProperty(description);
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

	public StringProperty descriptionProperty() {
		return this.description;
	}

	public java.lang.String getDescription() {
		return this.descriptionProperty().get();
	}

	public void setDescription(final java.lang.String description) {
		this.descriptionProperty().set(description);
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

	@Override
	public String toString() {
		return "Category [id=" + id + ", name=" + name + ", description=" + description + ", errorMessage="
				+ errorMessage + "]";
	}

}
