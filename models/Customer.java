package restaurant.models;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Customer {

	private LongProperty id;
	private StringProperty firstName;
	private StringProperty lastName;
	private StringProperty email;
	private StringProperty phoneNo;
	private StringProperty address;
	private StringProperty errorMessage = new SimpleStringProperty();

	public Customer(Long id, String firstName, String lastName, String email, String phoneNo, String address) {
		this.id = new SimpleLongProperty(id);
		this.firstName = new SimpleStringProperty(firstName);
		this.lastName = new SimpleStringProperty(lastName);
		this.email = new SimpleStringProperty(email);
		this.phoneNo = new SimpleStringProperty(phoneNo);
		this.address = new SimpleStringProperty(address);
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

	public StringProperty firstNameProperty() {
		return this.firstName;
	}

	public java.lang.String getFirstName() {
		return this.firstNameProperty().get();
	}

	public void setFirstName(final java.lang.String firstName) {
		this.firstNameProperty().set(firstName);
	}

	public StringProperty lastNameProperty() {
		return this.lastName;
	}

	public java.lang.String getLastName() {
		return this.lastNameProperty().get();
	}

	public void setLastName(final java.lang.String lastName) {
		this.lastNameProperty().set(lastName);
	}

	public StringProperty emailProperty() {
		return this.email;
	}

	public java.lang.String getEmail() {
		return this.emailProperty().get();
	}

	public void setEmail(final java.lang.String email) {
		this.emailProperty().set(email);
	}

	public StringProperty phoneNoProperty() {
		return this.phoneNo;
	}

	public java.lang.String getPhoneNo() {
		return this.phoneNoProperty().get();
	}

	public void setPhoneNo(final java.lang.String phoneNo) {
		this.phoneNoProperty().set(phoneNo);
	}

	public StringProperty addressProperty() {
		return this.address;
	}

	public java.lang.String getAddress() {
		return this.addressProperty().get();
	}

	public void setAddress(final java.lang.String address) {
		this.addressProperty().set(address);
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
		return "Customer [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", email=" + email
				+ ", phoneNo=" + phoneNo + ", address=" + address + ", errorMessage=" + errorMessage + "]";
	}

}
