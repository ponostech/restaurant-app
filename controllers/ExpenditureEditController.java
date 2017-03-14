package restaurant.controllers;

import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import restaurant.dao.ExpenditureDAO;
import restaurant.helpers.DateTimeHelper;
import restaurant.helpers.Message;
import restaurant.helpers.PopupHelper;
import restaurant.helpers.ScreenManager;
import restaurant.models.Expenditure;

public class ExpenditureEditController implements Initializable {
	@FXML
	private JFXTextField descText;

	@FXML
	private JFXTextField amountText;

	@FXML
	private JFXDatePicker dateSelect;

	@FXML
	private JFXButton saveButton;

	@FXML
	private JFXButton cancelButton;

	private Executor executor;
	private ExpenditureDAO expenditureDAO = new ExpenditureDAO();
	private Expenditure oldExpenditure = null;
	private int index = 0;
	private PopupHelper ph = new PopupHelper();

	private String dateFormat = "dd-MM-yyyy";
	private StringConverter<LocalDate> converter;

	// Populate expenditure data
	public void populateExpenditureData(Expenditure exp, int i) {
		oldExpenditure = exp;
		index = i;
		descText.setText(exp.getDescription());
		amountText.setText(String.valueOf(exp.getAmount()));
		LocalDate date = DateTimeHelper.formattedDate(exp.getDatetime());
		dateSelect.setValue(date);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		executor = Executors.newCachedThreadPool(runnable -> {
			Thread t = new Thread(runnable);
			t.setDaemon(true);
			return t;
		});

		dateSelect.setValue(LocalDate.now());

		converter = new StringConverter<LocalDate>() {

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);

			@Override
			public LocalDate fromString(String string) {
				if (string != null && !string.isEmpty()) {
					return LocalDate.parse(string, formatter);
				} else {
					return null;
				}
			}

			@Override
			public String toString(LocalDate date) {
				if (date != null) {
					return formatter.format(date);
				} else {
					return "";
				}
			}
		};

		dateSelect.setConverter(converter);

	}

	@FXML
	public void saveAction() throws NoSuchAlgorithmException, NoSuchProviderException {
		if (validate()) {

			String desc = descText.getText();
			String amount = amountText.getText();
			LocalDate date = dateSelect.getValue();
			LocalTime time = LocalTime.now();
			LocalDateTime dateTime = LocalDateTime.of(date, time);
			String dateTimeString = dateTime.toString();

			Expenditure expenditure = new Expenditure(oldExpenditure.getId(),
					ScreenManager.getCurrentUser().getUserId(), desc, Float.parseFloat(amount), dateTimeString);

			Task<Expenditure> task = new Task<Expenditure>() {

				@Override
				protected Expenditure call() throws Exception {
					return expenditureDAO.update(expenditure);
				}
			};

			task.setOnSucceeded(e -> {
				if (task.getValue().getErrorMessage() == null) {
					cancelAction();
					ph.showInfo(Message.EXPENDITURE_ADD);

					// Added new user to TableView
					ExpenditureController.updateItem(task.getValue(), index);

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
		errorMessage.add("Error: ");

		if (descText.getText().isEmpty()) {
			valid = false;
			errorMessage.add("Description");
		}
		if (amountText.getText().isEmpty()) {
			valid = false;
			errorMessage.add("Amount");
		} else {
			try {
				Float.parseFloat(amountText.getText().trim());
			} catch (NumberFormatException e) {
				valid = false;
				errorMessage.add("Invalid Amount value");
			}
		}
		if (dateSelect.getValue() == null) {
			valid = false;
			errorMessage.add("Date");
		}

		if (valid) {
			return true;
		} else {
			ph.showError(errorMessage.toString());
			return false;
		}
	}

}
