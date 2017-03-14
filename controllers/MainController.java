package restaurant.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import restaurant.dao.SettingDAO;
import restaurant.helpers.Settings;

public class MainController implements Initializable {

	@FXML
	private ScrollPane mainContainer;

	@FXML
	private GridPane tableContainer;

	private ArrayList<VBox> tables = new ArrayList<>();

	private SettingDAO settingDAO = new SettingDAO();

	String imagePath = "restaurant/images/table_small.png";
	Image image = new Image(imagePath);

	@Override
	public void initialize(URL location, ResourceBundle resource) {

		int noOfTables = Settings.NO_OF_TABLES;
		if (settingDAO.get("no_of_tables") != null) {
			noOfTables = Integer.parseInt(settingDAO.get("no_of_tables"));
		}

		initTables(noOfTables);

		for (VBox table : tables) {
			table.setOnMouseClicked(e -> {
				int tableNo = tables.indexOf(table) + 1;
				showInvoiceWindow(tableNo);
			});
		}

	}

	// Initialized Tables
	public void initTables(int noOfTables) {

		int row = 0;

		for (int i = 1; i <= noOfTables;) {

			for (int j = 0; j < 3; j++) {

				VBox table = new VBox(10);
				table.setAlignment(Pos.CENTER);
				table.setFocusTraversable(true);

				Label label = new Label("TABLE " + i);
				label.getStyleClass().add("btn-primary");
				label.setStyle("-fx-font-weight:bold; -fx-padding:10px 20px");

				ImageView imageView = new ImageView(image);
				imageView.setPreserveRatio(true);
				table.getChildren().addAll(imageView, label);

				GridPane.setConstraints(table, j, row, 1, 1, HPos.CENTER, VPos.CENTER, null, null, new Insets(20));
				tableContainer.getChildren().add(table);

				tables.add(table);

				i++;
				if (i > noOfTables)
					break;
			}
			row++;
		}

		tableContainer.setAlignment(Pos.TOP_LEFT);
		tableContainer.setMaxWidth(800);
	}
	
	// Show Invoice Window
	public void showInvoiceWindow(int tableNo) {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/restaurant/views/Invoice-Add.fxml"));
		try {
			ScrollPane root = loader.load();
			Stage window = new Stage();
			Scene scene = new Scene(root);
			
			InvoiceAddController invoiceAddController = loader.<InvoiceAddController> getController();
			invoiceAddController.setTableName(tableNo);
			
			scene.getStylesheets().add(getClass().getResource("/restaurant/application.css").toExternalForm());
			window.setScene(scene);
			window.setTitle("Invoice : TABLE " +  tableNo);
			window.getIcons().add(new Image("/restaurant/images/icon_add.png"));
			window.setResizable(false);
			window.show();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
