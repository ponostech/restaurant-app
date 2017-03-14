package restaurant.helpers;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PopupHelper {

	public void showError(String message) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/restaurant/views/Error-Popup.fxml"));
		try {
			VBox root = loader.load();

			// Get ErrorHelper Controller and Initialize message;
			ErrorHelper errorHelper = loader.<ErrorHelper> getController();
			errorHelper.setMessage(message);

			Stage window = new Stage();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/restaurant/application.css").toExternalForm());
			window.setScene(scene);
			window.setTitle("Error");
			window.initModality(Modality.APPLICATION_MODAL);
			window.setResizable(false);
			window.showAndWait();
		} catch (IOException e) {
		}
	}

	public void showInfo(String message) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/restaurant/views/Info-Popup.fxml"));
		try {
			VBox root = loader.load();

			// Get ErrorHelper Controller and Initialize message;
			InfoHelper infoHelper = loader.<InfoHelper> getController();
			infoHelper.setMessage(message);

			Stage window = new Stage();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/restaurant/application.css").toExternalForm());
			window.setScene(scene);
			window.setTitle("Information");
			window.initModality(Modality.APPLICATION_MODAL);
			window.setResizable(false);
			window.showAndWait();
		} catch (IOException e) {
		}
	}

	public boolean showConfirm(String message) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/restaurant/views/Confirm-Popup.fxml"));

		VBox root = loader.load();

		// Get ErrorHelper Controller and Initialize message;
		ConfirmHelper confirmHelper = loader.<ConfirmHelper> getController();
		confirmHelper.setMessage(message);

		Stage window = new Stage();
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("/restaurant/application.css").toExternalForm());
		window.setScene(scene);
		window.setTitle("Confirm Action?");
		window.initModality(Modality.APPLICATION_MODAL);
		window.setResizable(false);
		window.showAndWait();
		return confirmHelper.isConfirm();
	}
}
