package restaurant.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class DBSettingController implements Initializable {

	@FXML
	private JFXTextField dbHostText;

	@FXML
	private JFXTextField portText;

	@FXML
	private JFXTextField dbNameText;

	@FXML
	private JFXTextField usernameText;

	@FXML
	private JFXPasswordField passwordText;

	@FXML
	private JFXButton saveBtn;

	@FXML
	private JFXButton cancelBtn;

	@FXML
	void cancelAction() {
		Stage stage = (Stage) cancelBtn.getScene().getWindow();
		stage.close();
	}

	@FXML
	void cancelKeyPress(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			cancelAction();
		}
	}

	@FXML
	void saveAction() {
		Properties properties = new Properties();
		properties.setProperty("url", "jdbc:mysql://" + dbHostText.getText().trim() + ":" + portText.getText().trim()
				+ "/" + dbNameText.getText().trim());
		properties.setProperty("username", usernameText.getText().trim());
		properties.setProperty("password", passwordText.getText().trim());

		try {
			properties.store(
					new FileOutputStream(System.getProperty("user.dir") + File.separator + "database.properties"),
					"Database");
			cancelAction();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		}

	}

	@FXML
	void saveKeyPress(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			saveAction();
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Properties properties = new Properties();
		try {
			properties
					.load(new FileInputStream(System.getProperty("user.dir") + File.separator + "database.properties"));

			String dbUrl = properties.getProperty("url");
			String[] dbUrlArray = dbUrl.split(":");
			String[] dbPortAndName = dbUrlArray[3].split("/");

			String dbHost = dbUrlArray[2].replace("//", "");
			String dbPort = dbPortAndName[0];
			String dbName = dbPortAndName[1];
			String dbUser = properties.getProperty("username");
			String dbPassword = properties.getProperty("password");
			dbHostText.setText(dbHost);
			portText.setText(dbPort);
			dbNameText.setText(dbName);
			usernameText.setText(dbUser);
			passwordText.setText(dbPassword);

		} catch (FileNotFoundException e) {
			dbHostText.setText("localhost");
			portText.setText("3306");
			dbNameText.setText("ponos_restaurant");
			usernameText.setText("root");
			passwordText.setText("");

		} catch (IOException e) {

		}

	}

}
