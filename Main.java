package restaurant;
	

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import restaurant.helpers.ScreenManager;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {		
		Scene scene = new Scene(new StackPane());
		scene.getStylesheets().add(getClass().getResource("/restaurant/application.css").toExternalForm());
		ScreenManager screenManager = new ScreenManager(scene);
		screenManager.showLoginScreen();
		primaryStage.initStyle(StageStyle.UNDECORATED);
		primaryStage.getIcons().add(new Image("/restaurant/images/logo.png"));
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
