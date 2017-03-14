package restaurant.controllers;

import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import restaurant.dao.UserDAO;
import restaurant.helpers.Message;
import restaurant.helpers.PasswordHelper;
import restaurant.helpers.PopupHelper;
import restaurant.models.User;

public class UserAddController implements Initializable {

	@FXML
	private JFXTextField usernameText;

	@FXML
	private JFXTextField passwordText;

	@FXML
	private JFXTextField firstNameText;

	@FXML
	private JFXTextField lastNameText;

	@FXML
	private JFXTextField emailText;

	@FXML
	private JFXTextField phoneNoText;

	@FXML
	private JFXTextField addressText;

	@FXML
	private JFXComboBox<String> roleSelect;

	@FXML
	private JFXButton saveButton;

	@FXML
	private JFXButton cancelButton;

	private Executor executor;
	private UserDAO userDAO = new UserDAO();
	private PopupHelper ph = new PopupHelper();

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		executor = Executors.newCachedThreadPool(runnable -> {
			Thread t = new Thread(runnable);
			t.setDaemon(true);
			return t;
		});

		// Populate role select items
		roleSelect.getItems().addAll("ADMINISTRATOR", "EMPLOYEE");
		roleSelect.setValue("EMPLOYEE");

		// Remove white space from userName
		usernameText.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.SPACE) {
				ph.showError(Message.WHITE_SPACE_ERROR);
			}
		});
	}

	@FXML
	public void saveAction() throws NoSuchAlgorithmException, NoSuchProviderException {
		if (validate()) {
			Integer roleValue = 1;
			String username = usernameText.getText().trim();
			PasswordHelper passwordHelper = new PasswordHelper(passwordText.getText().trim());
			String password = passwordHelper.getGeneratedPassword();
			String firstName = firstNameText.getText();
			String lastName = lastNameText.getText();
			String email = emailText.getText();
			String address = addressText.getText();
			String phoneNo = phoneNoText.getText();
			String role = roleSelect.getValue();
			if (role.equals("EMPLOYEE"))
				roleValue = 2;

			User user = new User(0, username, password, firstName, lastName, email, phoneNo, address, roleValue);

			Task<User> task = new Task<User>() {

				@Override
				protected User call() throws Exception {
					return userDAO.save(user);
				}
			};

			task.setOnSucceeded(e -> {
				if (task.getValue().getErrorMessage() == null) {
					clearFields();
					ph.showInfo(Message.USER_ADD);
					usernameText.requestFocus();

					// Added new user to TableView
					UserController.addItem(task.getValue());

				} else {
					ph.showError(task.getValue().getErrorMessage());
				}
			});

			executor.execute(task);
		}
	}

	@FXML
	public void cancelAction() {
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		stage.close();
	}

	@FXML
	public void saveKeyPress(KeyEvent event) throws NoSuchAlgorithmException, NoSuchProviderException {
		if (event.getCode() == KeyCode.ENTER) {
			saveAction();
		}
	}

	@FXML
	public void cancelKeyPress(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			cancelAction();
		}
	}

	// Validate User data
	public boolean validate() {
		boolean valid = true;
		StringJoiner errorMessage = new StringJoiner(", ");
		errorMessage.add("Mandatory Fields: ");

		if (usernameText.getText().isEmpty()) {
			valid = false;
			errorMessage.add("Username");
		}
		if (passwordText.getText().isEmpty()) {
			valid = false;
			errorMessage.add("Password");
		}

		if (valid) {
			return true;
		} else {
			ph.showError(errorMessage.toString());
			return false;
		}
	}

	// Clear user fields
	public void clearFields() {
		usernameText.setText("");
		passwordText.setText("");
		firstNameText.setText("");
		lastNameText.setText("");
		emailText.setText("");
		phoneNoText.setText("");
		addressText.setText("");
		roleSelect.setValue("ADMINISTRATOR");
	}

}
