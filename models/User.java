package restaurant.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class User {

	private IntegerProperty userId;
	private StringProperty username;
	private StringProperty password;
	private StringProperty firstName;
	private StringProperty lastName;
	private StringProperty email;
	private StringProperty phoneNo;
	private StringProperty address;
	private IntegerProperty role;
	private StringProperty errorMessage = new SimpleStringProperty();

	public User(Integer userId, String username, String password, String firstName, String lastName, String email,
			String phoneNo, String address, Integer role) {
		this.userId = new SimpleIntegerProperty(userId);
		this.username = new SimpleStringProperty(username);
		this.password = new SimpleStringProperty(password);
		this.firstName = new SimpleStringProperty(firstName);
		this.lastName = new SimpleStringProperty(lastName);
		this.email = new SimpleStringProperty(email);
		this.phoneNo = new SimpleStringProperty(phoneNo);
		this.address = new SimpleStringProperty(address);
		this.role = new SimpleIntegerProperty(role);
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

	public StringProperty usernameProperty() {
		return this.username;
	}

	public String getUsername() {
		return this.usernameProperty().get();
	}

	public void setUsername(final String username) {
		this.usernameProperty().set(username);
	}

	public StringProperty passwordProperty() {
		return this.password;
	}

	public String getPassword() {
		return this.passwordProperty().get();
	}

	public void setPassword(final String password) {
		this.passwordProperty().set(password);
	}

	public StringProperty firstNameProperty() {
		return this.firstName;
	}

	public String getFirstName() {
		return this.firstNameProperty().get();
	}

	public void setFirstName(final String firstName) {
		this.firstNameProperty().set(firstName);
	}

	public StringProperty lastNameProperty() {
		return this.lastName;
	}

	public String getLastName() {
		return this.lastNameProperty().get();
	}

	public void setLastName(final String lastName) {
		this.lastNameProperty().set(lastName);
	}

	public StringProperty emailProperty() {
		return this.email;
	}

	public String getEmail() {
		return this.emailProperty().get();
	}

	public void setEmail(final String email) {
		this.emailProperty().set(email);
	}

	public StringProperty addressProperty() {
		return this.address;
	}

	public String getAddress() {
		return this.addressProperty().get();
	}

	public void setAddress(final String address) {
		this.addressProperty().set(address);
	}

	public StringProperty phoneNoProperty() {
		return this.phoneNo;
	}

	public String getPhoneNo() {
		return this.phoneNoProperty().get();
	}

	public void setPhoneNo(final String phoneNo) {
		this.phoneNoProperty().set(phoneNo);
	}

	public IntegerProperty roleProperty() {
		return this.role;
	}

	public int getRole() {
		return this.roleProperty().get();
	}

	public void setRole(final int role) {
		this.roleProperty().set(role);
	}

	public String getErrorMessage() {
		return this.errorMessageProperty().get();
	}

	public void setErrorMessage(final String errorMessage) {
		this.errorMessageProperty().set(errorMessage);
	}

	public StringProperty errorMessageProperty() {
		return this.errorMessage;
	}

	@Override
	public String toString() {
		return "User [userId=" + userId + ", username=" + username + ", password=" + password + ", firstName="
				+ firstName + ", lastName=" + lastName + ", email=" + email + ", address=" + address + ", phoneNo="
				+ phoneNo + ", role=" + role + "]";
	}

}
