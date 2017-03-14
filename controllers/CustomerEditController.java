package restaurant.controllers;

import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import restaurant.dao.CustomerDAO;
import restaurant.helpers.Message;
import restaurant.helpers.PopupHelper;
import restaurant.models.Customer;

public class CustomerEditController implements Initializable{
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
	private JFXButton saveButton;

	@FXML
	private JFXButton cancelButton;

	private Executor executor;	
	private CustomerDAO customerDAO = new CustomerDAO();
	private PopupHelper ph = new PopupHelper();
	
	Customer oldCustomer = null;
	int index = 0;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		executor = Executors.newCachedThreadPool(runnable -> {
			Thread t = new Thread(runnable);
			t.setDaemon(true);
			return t;
		});
		
	}
	
	// Populate Customer Data
	public void populateCustomerData(Customer customer, int i) {
		oldCustomer = customer;
		index = i;
		firstNameText.setText(customer.getFirstName());
		lastNameText.setText(customer.getLastName());
		emailText.setText(customer.getEmail());
		phoneNoText.setText(customer.getPhoneNo());
		addressText.setText(customer.getAddress());
	}

	@FXML
	public void saveAction() throws NoSuchAlgorithmException, NoSuchProviderException {
		if (validate()) {
			
			String firstName = firstNameText.getText();
			String lastName = lastNameText.getText();
			String email = emailText.getText();
			String address = addressText.getText();
			String phoneNo = phoneNoText.getText();

			Customer customer = new Customer(oldCustomer.getId(), firstName, lastName, email, phoneNo, address);

			Task<Customer> task = new Task<Customer>() {

				@Override
				protected Customer call() throws Exception {
					return customerDAO.update(customer);
				}
			};

			task.setOnSucceeded(e -> {
				if (task.getValue().getErrorMessage() == null) {
					ph.showInfo(Message.CUSTOMER_UPDATE);
					cancelAction();

					// Added new user to TableView
					CustomerController.updateItem(task.getValue(), index);

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

		if (firstNameText.getText().isEmpty() ) {
			valid = false;
			errorMessage.add("First Name");
		}
		if (addressText.getText().isEmpty()) {
			valid = false;
			errorMessage.add("Address");
		}

		if (valid) {
			return true;
		} else {
			ph.showError(errorMessage.toString());
			return false;
		}
	}

}
