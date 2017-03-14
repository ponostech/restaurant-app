package restaurant.controllers;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.jfoenix.controls.JFXTextField;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import restaurant.dao.SettingDAO;
import restaurant.helpers.Message;
import restaurant.helpers.PopupHelper;
import restaurant.helpers.Settings;

public class SettingsController implements Initializable {

	@FXML
	private JFXTextField nameText;

	@FXML
	private JFXTextField address1Text;

	@FXML
	private JFXTextField address2Text;

	@FXML
	private JFXTextField mobNoText;

	@FXML
	private JFXTextField storeNoText;

	@FXML
	private JFXTextField tableNoText;

	@FXML
	private JFXTextField taxText;

	@FXML
	private JFXTextField serviceChargeText;

	@FXML
	private JFXTextField packingChargeText;

	public Map<String, JFXTextField> keyControlMap = new HashMap<String, JFXTextField>();

	private static SettingDAO settingDAO = new SettingDAO();
	private PopupHelper ph = new PopupHelper();

	private Executor executor;

	@FXML
	public void saveAction() {

		if (validate()) {

			Map<String, String> settings = new HashMap<>();

			for (Map.Entry<String, JFXTextField> setting : keyControlMap.entrySet()) {
				settings.put(setting.getKey(), setting.getValue().getText());
			}

			Task<Boolean> task = new Task<Boolean>() {

				@Override
				protected Boolean call() throws Exception {
					return settingDAO.save(settings);
				}
			};

			task.setOnSucceeded(e -> {
				if (task.getValue()) {
					ph.showInfo(Message.SETTING_UPDATE);
					FXMLLoader headerLoader = new FXMLLoader(getClass().getResource("/restaurant/views/Header.fxml"));
					try {
						headerLoader.load();
						HeaderController headerController = headerLoader.<HeaderController> getController();
						headerController.setName(nameText.getText());

					} catch (Exception e1) {
						e1.printStackTrace();
					}

				} else {
					ph.showError(Message.ERROR);
				}
			});

			executor.execute(task);
		}
	}

	@FXML
	public void saveKeyPress(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			saveAction();
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// Create Executor thread
		executor = Executors.newCachedThreadPool(runnable -> {
			Thread t = new Thread(runnable);
			t.setDaemon(true);
			return t;
		});

		keyControlMap.put("name", nameText);
		keyControlMap.put("address1", address1Text);
		keyControlMap.put("address2", address2Text);
		keyControlMap.put("mobile_no", mobNoText);
		keyControlMap.put("store_no", storeNoText);
		keyControlMap.put("no_of_tables", tableNoText);
		keyControlMap.put("tax", taxText);
		keyControlMap.put("service_charge", serviceChargeText);
		keyControlMap.put("packing_charge", packingChargeText);

		for (Map.Entry<String, String> setting : Settings.defaultSettings.entrySet()) {
			populateSettings(setting.getKey(), setting.getValue());
		}

	}

	// Populate Setting
	public void populateSettings(String key, String defaultValue) {
		String currentValue = settingDAO.get(key);
		if (currentValue != null) {
			keyControlMap.get(key).setText(currentValue);
		} else {
			keyControlMap.get(key).setText(defaultValue);
		}
	}

	// Validate Settings
	public boolean validate() {
		boolean valid = true;
		StringJoiner sj = new StringJoiner(", ");

		if (!tableNoText.getText().isEmpty()) {
			try {
				Integer.parseInt(tableNoText.getText().trim());
			} catch (NumberFormatException e) {
				valid = false;
				sj.add("Invalid value of number of Tables");
			}
		}

		if (!taxText.getText().isEmpty()) {
			try {
				Float.parseFloat(taxText.getText().trim());
			} catch (NumberFormatException e) {
				valid = false;
				sj.add("Invalid value of Tax");
			}
		}

		if (!serviceChargeText.getText().isEmpty()) {
			try {
				Float.parseFloat(serviceChargeText.getText().trim());
			} catch (NumberFormatException e) {
				valid = false;
				sj.add("Invalid value of Service Charge");
			}
		}

		if (!packingChargeText.getText().isEmpty()) {
			try {
				Float.parseFloat(packingChargeText.getText().trim());
			} catch (NumberFormatException e) {
				valid = false;
				sj.add("Invalid value of Packing Charge");
			}
		}

		if (!valid) {
			ph.showError("Errors: " + sj.toString());
			return valid;
		}

		return valid;
	}

	public static String getSetting(String name) {
		String value = settingDAO.get(name);
		if (value != null) {
			return value;
		} else {
			return Settings.defaultSettings.get(name);
		}
	}

}
