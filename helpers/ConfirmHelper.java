package restaurant.helpers;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class ConfirmHelper implements Initializable {
	@FXML
	private Label messageText;

	@FXML
	private JFXButton confirmButton;

	@FXML
	private JFXButton closeButton;

	public boolean confirm = false;

	public void setMessage(String message) {

		messageText.setText(message);

		confirmButton.setOnAction(e -> {
			Stage stage = (Stage) closeButton.getScene().getWindow();
			stage.close();
			setConfirm(true);
		});

		confirmButton.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				Stage stage = (Stage) closeButton.getScene().getWindow();
				stage.close();
				setConfirm(true);
			}
		});

		closeButton.setOnAction(e -> {
			Stage stage = (Stage) closeButton.getScene().getWindow();
			stage.close();
			setConfirm(false);

		});

		closeButton.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				Stage stage = (Stage) closeButton.getScene().getWindow();
				stage.close();
				setConfirm(false);
			}
		});
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

	public boolean isConfirm() {
		return confirm;
	}

	public void setConfirm(boolean confirm) {
		this.confirm = confirm;
	}
}
