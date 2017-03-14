package restaurant.controllers;

import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import restaurant.dao.CategoryDAO;
import restaurant.helpers.Message;
import restaurant.helpers.PopupHelper;
import restaurant.models.Category;

public class CategoryEditController implements Initializable {

	@FXML
	private JFXTextField nameText;

	@FXML
	private JFXTextArea descText;

	@FXML
	private JFXButton saveButton;

	@FXML
	private JFXButton cancelButton;

	private Executor executor;
	private CategoryDAO categoryDAO = new CategoryDAO();
	private PopupHelper ph = new PopupHelper();
	private Category oldCategory = null;
	private int index = 0;

	// Populate Category Data
	public void populateCategoryData(Category category, int i) {
		oldCategory = category;
		index = i;
		nameText.setText(category.getName());
		descText.setText(category.getDescription());
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		executor = Executors.newCachedThreadPool(runnable -> {
			Thread t = new Thread(runnable);
			t.setDaemon(true);
			return t;
		});

	}

	@FXML
	public void saveAction() throws NoSuchAlgorithmException, NoSuchProviderException {
		if (validate()) {

			String name = nameText.getText();
			String desc = descText.getText();

			Category category = new Category(oldCategory.getId(), name, desc);

			Task<Category> task = new Task<Category>() {

				@Override
				protected Category call() throws Exception {
					return categoryDAO.update(category);
				}
			};

			task.setOnSucceeded(e -> {
				if (task.getValue().getErrorMessage() == null) {
					cancelAction();
					ph.showInfo(Message.CATEGORY_UPDATE);

					// Added new user to TableView
					CategoryController.updateItem(task.getValue(), index);

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

		if (nameText.getText().isEmpty()) {
			valid = false;
			errorMessage.add("Name");
		}
		if (valid) {
			return true;
		} else {
			ph.showError(errorMessage.toString());
			return false;
		}
	}

}
