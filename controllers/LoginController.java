package restaurant.controllers;

import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.mysql.jdbc.Connection;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import restaurant.dao.Database;
import restaurant.dao.UserDAO;
import restaurant.helpers.Message;
import restaurant.helpers.PasswordHelper;
import restaurant.helpers.ScreenManager;
import restaurant.models.User;

public class LoginController implements Initializable {

	@FXML
	private Pane loginScene;

	@FXML
	private Label loginStatusLabel;

	@FXML
	private JFXTextField usernameText;

	@FXML
	private JFXPasswordField passwordText;

	@FXML
	private JFXButton loginBtn;

	@FXML
	private JFXButton exitBtn;

	@FXML
	private Label dbStatusLabel;

	@FXML
	private Hyperlink dbSettingLink;
	
	String imagePath = "restaurant/images/login_bg.jpg";
	Image image = new Image(imagePath);

	private ScreenManager screenManager;
	UserDAO userDAO = new UserDAO();

	public void initLogin(final ScreenManager sm) {
		screenManager = sm;
		loginBtn.setOnAction(e -> {
			try {
				doLogin();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
	}

	public void doLogin() throws NoSuchAlgorithmException, NoSuchProviderException, SQLException {
		User user = authorize();
		if (user != null) {
			Stage stage = (Stage) loginBtn.getScene().getWindow();
			stage.close();
			screenManager.authenticated(user);
		} else {
			loginStatusLabel.setText(Message.LOGIN_ERROR);
		}
	}

	public User authorize() throws NoSuchAlgorithmException, NoSuchProviderException, SQLException {
		String username = usernameText.getText().trim();
		PasswordHelper passwordHelper = new PasswordHelper(passwordText.getText().trim());
		String password = passwordHelper.getGeneratedPassword();
		return userDAO.get(username, password);
	}

	@FXML
	public void loginKeyPress(KeyEvent event) throws NoSuchAlgorithmException, NoSuchProviderException, SQLException {
		if (event.getCode() == KeyCode.ENTER) {
			doLogin();
		}
	}

	@FXML
	public void passwordKeyPress(KeyEvent event)
			throws NoSuchAlgorithmException, NoSuchProviderException, SQLException {
		if (event.getCode() == KeyCode.ENTER) {
			doLogin();
		}
	}

	@FXML
	public void usernameKeyPress(KeyEvent event) {
		clearLoginError();
	}

	@FXML
	public void dbSettingAction() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/restaurant/views/Database-Settings.fxml"));

		try {
			Pane root = loader.load();
			Stage stage = new Stage();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/restaurant/application.css").toExternalForm());
			stage.setScene(scene);
			stage.initStyle(StageStyle.UNDECORATED);
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.showAndWait();
			checkDBConnection();
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	@FXML
	public void exitAction() {
		Stage stage = (Stage) exitBtn.getScene().getWindow();
		stage.close();

	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		BackgroundImage myBI= new BackgroundImage(image,
		        BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT,
		          BackgroundSize.DEFAULT);
		//then you set to your node
		loginScene.setBackground(new Background(myBI));
		
		loginBtn.setDisable(true);
		checkDBConnection();

	}

	public void checkDBConnection() {
		clearLoginError();
		Task<Boolean> task = new Task<Boolean>() {
			boolean connected = true;

			@Override
			protected Boolean call() throws Exception {
				// Check Database Connection
				try {
					Database dbObj = new Database();
					Connection conn = dbObj.getConnection();
					if (conn == null) {
						connected = false;
					}
				} catch (SQLException e) {
				}

				return connected;
			}
		};

		task.setOnSucceeded(e -> {
			if (task.getValue()) {
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						loginBtn.setDisable(false);
						usernameText.requestFocus();
						dbStatusLabel.setText(Message.DB_CONN_SUCCESS);
						dbStatusLabel.setStyle("-fx-text-fill:#44B449");
					}
				});

			} else {
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						dbStatusLabel.setText(Message.DB_CONN_ERROR);
						dbStatusLabel.setStyle("-fx-text-fill:#ff0000");
					}
				});

			}
		});

		Thread t = new Thread(task);
		t.setDaemon(true);
		t.start();

		// return connected;
	}

	public void clearLoginError() {
		loginStatusLabel.setText("");
	}

}
