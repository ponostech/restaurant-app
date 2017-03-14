package restaurant.controllers;

import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
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
import restaurant.dao.CategoryDAO;
import restaurant.dao.ItemDAO;
import restaurant.helpers.Message;
import restaurant.helpers.PopupHelper;
import restaurant.helpers.ScreenManager;
import restaurant.models.Category;
import restaurant.models.Item;

public class ItemEditController implements Initializable {
	@FXML
	private JFXTextField nameText;

	@FXML
	private JFXTextField priceText;

	@FXML
	private JFXTextField shortcutText;

	@FXML
	private JFXComboBox<String> categorySelect;

	@FXML
	private JFXButton saveButton;

	@FXML
	private JFXButton cancelButton;

	private Executor executor;
	private ItemDAO itemDAO = new ItemDAO();
	private CategoryDAO categoryDAO = new CategoryDAO();
	private ArrayList<Category> categories = new ArrayList<>();
	private PopupHelper ph = new PopupHelper();

	private Item oldItem = null;
	private int index = 0;

	// Populate Menu Item data
	public void populateItemData(Item item, int i) {
		boolean isCategorized = false;
		oldItem = item;
		index = i;
		nameText.setText(item.getName());
		priceText.setText(Float.toString(item.getPrice()));
		shortcutText.setText(Integer.toString(item.getShortCutKey()));

		for (Category category : categories) {
			if (item.getCategoryId() == category.getId()) {
				categorySelect.setValue(category.getName());
				isCategorized = true;
				break;
			}
		}

		if (!isCategorized)
			categorySelect.setValue("Uncategorized");

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		executor = Executors.newCachedThreadPool(runnable -> {
			Thread t = new Thread(runnable);
			t.setDaemon(true);
			return t;
		});

		// Populate category select items
		categories = categoryDAO.getCategories();
		categories.add(0, new Category(0, "Uncategorized", ""));
		for (Category category : categories) {
			categorySelect.getItems().add(category.getName());
		}
	}

	@FXML
	public void saveAction() throws NoSuchAlgorithmException, NoSuchProviderException {
		if (validate()) {

			String name = nameText.getText().trim();
			Float price = Float.parseFloat(priceText.getText());
			Category category = categories.get(categorySelect.getSelectionModel().getSelectedIndex());
			Integer sKey = 1;

			if (!shortcutText.getText().trim().isEmpty()) {
				sKey = Integer.parseInt(shortcutText.getText().trim());

			}

			Item item = new Item(oldItem.getId(), ScreenManager.getCurrentUser().getUserId(), name, price, sKey, category.getId());

			Task<Item> task = new Task<Item>() {

				@Override
				protected Item call() throws Exception {
					return itemDAO.update(item);
				}
			};

			task.setOnSucceeded(e -> {
				if (task.getValue().getErrorMessage() == null) {
					cancelAction();
					ph.showInfo(Message.ITEM_UPDATE);

					// Added new user to TableView
					ItemController.updateItem(task.getValue(), index);

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

		if (nameText.getText().isEmpty()) {
			valid = false;
			errorMessage.add("Item Name");
		}
		if (priceText.getText().isEmpty()) {
			valid = false;
			errorMessage.add("Price");
		} else {
			try {
				Float.parseFloat(priceText.getText().trim());
			} catch (NumberFormatException e) {
				valid = false;
				errorMessage.add("Invalid Price format");
			}
		}
		if (shortcutText.getText().isEmpty()) {
			valid = false;
			errorMessage.add("Shortcut Key");
		} else {
			try {
				Integer.parseInt(shortcutText.getText().trim());
			} catch (NumberFormatException e) {
				valid = false;
				errorMessage.add("Invalid Shortcut Key");
			}

		}

		if (valid) {
			return true;
		} else {
			ph.showError(errorMessage.toString());
			return false;
		}
	}
}
