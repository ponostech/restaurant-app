package restaurant.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import restaurant.models.User;

public class UserViewController implements Initializable {

	@FXML
	private Label usernameLabel;

	@FXML
	private Label firstNameLabel;

	@FXML
	private Label lastNameLabel;

	@FXML
	private Label emailLabel;

	@FXML
	private Label phoneNoLabel;

	@FXML
	private Label roleLabel;

	@FXML
	private Label addressLabel;

	// Populate User Data
	public void populateUserData(User user) {
		usernameLabel.setText(user.getUsername());
		firstNameLabel.setText(user.getFirstName());
		lastNameLabel.setText(user.getLastName());
		emailLabel.setText(user.getEmail());
		phoneNoLabel.setText(user.getPhoneNo());
		addressLabel.setText(user.getAddress());

		if (user.getRole() == 1) {
			roleLabel.setText("ADMINISTRATOR");
		} else {
			roleLabel.setText("EMPLOYEE");
		}

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

}
