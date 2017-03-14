package restaurant.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import restaurant.Main;
import restaurant.dao.SettingDAO;
import restaurant.helpers.ScreenManager;
import restaurant.helpers.Settings;

public class HeaderController implements Initializable {

	@FXML
	public Label storeName;

	@FXML
	private Label usernameLabel;

	@FXML
	private FontAwesomeIconView logoutButton;
	
	private SettingDAO settingDAO = new SettingDAO();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		String name = settingDAO.get("name");
		if(name != null ) {
			storeName.setText(name);
		}else {
			storeName.setText(Settings.APP_NAME);
		}

		usernameLabel.setText(ScreenManager.getCurrentUser().getUsername());

		logoutButton.setOnMouseClicked(e -> {
			Stage curStage = (Stage) ScreenManager.getScene().getWindow();
			ScreenManager.setRoot(null);
			curStage.close();
			ScreenManager.setRoot(new BorderPane());
			Main main = new Main();
			try {
				main.init();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			main.start(new Stage());
		});
	}
	
	public void setName(String name) {
		//System.out.println(name);
		//new PopupHelper().showInfo(name);
		storeName.setText(name);
	}

}
