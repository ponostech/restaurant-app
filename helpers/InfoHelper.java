package restaurant.helpers;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class InfoHelper implements Initializable{
	@FXML
    private Label messageText;

    @FXML
    private JFXButton closeButton;

    @FXML
    public void closeAction() {
    	Stage stage = (Stage) closeButton.getScene().getWindow();
    	stage.close();
    }

    @FXML
    public void closeKeyPress(KeyEvent event) {
    	if(event.getCode() == KeyCode.ENTER) {
    		closeAction();
    	}
    }
    
    public void setMessage(String message) {
    	messageText.setText(message);
    }

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		
	}
}
