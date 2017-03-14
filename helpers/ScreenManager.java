package restaurant.helpers;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import restaurant.controllers.LoginController;
import restaurant.models.User;

public class ScreenManager {

	private static User currentUser;
	private static Scene scene;
	private static BorderPane root = new BorderPane();

	public ScreenManager(Scene scene) {
		ScreenManager.scene = scene;
	}

	public static User getCurrentUser() {
		return currentUser;
	}

	public static void setUser(User user) {
		currentUser = user;
	}

	public static Scene getScene() {
		return scene;
	}

	public static BorderPane getRoot() {
		return root;
	}

	public static void setRoot(BorderPane node) {
		root = node;
	}

	public void authenticated(User user) {
		currentUser = user;
		showMainScreen();
	}

	public void showLoginScreen() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/restaurant/views/Login.fxml"));
			Pane root = loader.load();
			scene.setRoot(root);
			LoginController loginController = loader.<LoginController> getController();
			loginController.initLogin(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void showMainScreen() {
		try {
			ScrollPane main = null;
			
			FXMLLoader headerLoader = new FXMLLoader(getClass().getResource("/restaurant/views/Header.fxml"));
			VBox header = headerLoader.load();

			FXMLLoader sideMenuLoader = new FXMLLoader(getClass().getResource("/restaurant/views/Side-Menu.fxml"));
			ScrollPane sideMenu = sideMenuLoader.load();

			FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/restaurant/views/Main.fxml"));
			main = mainLoader.load();

			FXMLLoader footerLoader = new FXMLLoader(getClass().getResource("/restaurant/views/Footer.fxml"));
			HBox footer = footerLoader.load();

			root.setTop(header);
			root.setLeft(sideMenu);
			root.setCenter(main);
			root.setBottom(footer);
			
			main.setFitToHeight(true);
			main.setFitToWidth(true);
			BorderPane.setAlignment(main, Pos.TOP_LEFT);

			scene.setRoot(root);
			scene.getStylesheets().add(getClass().getResource("/restaurant/application.css").toExternalForm());

			Stage stage = new Stage();
			stage.setScene(scene);
			stage.setTitle("Restaurant Management System : PONOS Tech.");
			stage.getIcons().add(new Image("/restaurant/images/logo.png"));
			stage.setMinWidth(1280);
			stage.setMinHeight(800);
			stage.setMaximized(true);
			stage.show();

		} catch (IOException e) {
		}

	}

}
