package restaurant.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import restaurant.helpers.ScreenManager;

public class MenuController implements Initializable {

	@FXML
	private HBox mainMenu;

	@FXML
	private HBox orderMenu;

	@FXML
	private HBox invoiceMenu;

	@FXML
	private HBox itemMenu;

	@FXML
	private HBox categoryMenu;

	@FXML
	private HBox expenditureMenu;

	@FXML
	private HBox customerMenu;

	@FXML
	private HBox userMenu;

	@FXML
	private HBox reportMenu;

	@FXML
	private HBox settingMenu;

	@FXML
	public void mainMenuClick() {
		URL location = getClass().getResource("/restaurant/views/Main.fxml");
		loadMainLayout(location);
	}

	@FXML
	public void orderMenuClick() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/restaurant/views/Invoice-Add.fxml"));
		try {
			ScrollPane root = loader.load();
			Stage window = new Stage();
			Scene scene = new Scene(root);

			InvoiceAddController invoiceAddController = loader.<InvoiceAddController> getController();
			invoiceAddController.setTableName(0);

			scene.getStylesheets().add(getClass().getResource("/restaurant/application.css").toExternalForm());
			window.setScene(scene);
			window.setTitle("Invoice : Order/Take Out");
			window.getIcons().add(new Image("/restaurant/images/icon_add.png"));
			window.setResizable(false);
			window.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void invoiceMenuClick() {
		URL location = getClass().getResource("/restaurant/views/Invoices.fxml");
		loadMainLayout(location);
	}

	@FXML
	public void itemMenuClick() {
		URL location = getClass().getResource("/restaurant/views/Items.fxml");
		loadMainLayout(location);
	}

	@FXML
	public void categoryMenuClick() {
		URL location = getClass().getResource("/restaurant/views/Categories.fxml");
		loadMainLayout(location);
	}

	@FXML
	public void expenditureMenuClick() {
		URL location = getClass().getResource("/restaurant/views/Expenses.fxml");
		loadMainLayout(location);
	}

	@FXML
	public void customerMenuClick() {
		URL location = getClass().getResource("/restaurant/views/Customers.fxml");
		loadMainLayout(location);
	}

	@FXML
	public void userMenuClick() {
		URL location = getClass().getResource("/restaurant/views/Users.fxml");
		loadMainLayout(location);
	}

	@FXML
	public void reportMenuClick() {
		URL location = getClass().getResource("/restaurant/views/Reports.fxml");
		loadMainLayout(location);
	}

	@FXML
	public void settingMenuClick() {
		URL location = getClass().getResource("/restaurant/views/Settings.fxml");
		loadMainLayout(location);
	}

	public void loadMainLayout(URL location) {
		FXMLLoader loader = new FXMLLoader(location);
		try {
			ScrollPane main = loader.load();
			Double width = ScreenManager.getScene().getWidth();
			Double height = ScreenManager.getScene().getHeight();
			main.setPrefViewportWidth(width);
			main.setPrefViewportWidth(height);

			// Resize scroll pane on window size change
			ScreenManager.getScene().widthProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					main.setPrefViewportWidth((double) newValue);
				}
			});

			// Resize scroll pane on window size change
			ScreenManager.getScene().heightProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					main.setPrefViewportHeight((double) newValue);
				}
			});

			ScreenManager.getRoot().setCenter(main);
			main.setFitToHeight(true);
			main.setFitToWidth(true);
			BorderPane.setAlignment(main, Pos.TOP_LEFT);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

	}

}
