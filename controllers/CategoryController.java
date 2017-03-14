package restaurant.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import restaurant.dao.CategoryDAO;
import restaurant.helpers.Message;
import restaurant.helpers.PopupHelper;
import restaurant.helpers.ScreenManager;
import restaurant.helpers.Settings;
import restaurant.models.Category;

public class CategoryController implements Initializable {

	@FXML
	private VBox tableWrapper;

	@FXML
	private Button addButton;

	@FXML
	private Button editButton;

	@FXML
	private Button deleteButton;

	@FXML
	private Button refreshButton;

	// User Table
	private static TableView<Category> table;
	private static TableColumn<Category, String> idCol;
	private static TableColumn<Category, String> nameCol;
	private static TableColumn<Category, String> descCol;
	private static StackPane paginationPane;
	private static Pagination pagination;
	private static ObservableList<Category> categories = FXCollections.observableArrayList();
	private static IntegerProperty limit = new SimpleIntegerProperty(Settings.ROWS_PER_PAGE_1);

	private CategoryDAO categoryDAO = new CategoryDAO();
	private Executor executor;
	PopupHelper ph = new PopupHelper();

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// Create Executor thread
		executor = Executors.newCachedThreadPool(runnable -> {
			Thread t = new Thread(runnable);
			t.setDaemon(true);
			return t;
		});

		// Initialize Table and Pagination
		initTablePagination();
		populateTable();

		// Bind Table Columns with KeyPress Action Event
		table.setOnKeyPressed(e -> {

			if (e.getCode() == KeyCode.E) {
				editAction();
			}

			if (e.getCode() == KeyCode.DELETE) {
				try {
					deleteAction();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		table.setOnMouseClicked(e -> {

			if (e.getButton().equals(MouseButton.SECONDARY)) {
				final ContextMenu menu = new ContextMenu();
				MenuItem edit = new MenuItem("Edit Category");
				MenuItem delete = new MenuItem("Delete Category");

				edit.setOnAction(e1 -> {
					editAction();
				});

				delete.setOnAction(e2 -> {
					try {
						deleteAction();
					} catch (Exception e4) {
						e4.printStackTrace();
					}
				});

				menu.getItems().addAll(edit, delete);
				table.setContextMenu(menu);
			}
		});

		// Add Listener to Pagination
		pagination.currentPageIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				setTablePagination(newValue.intValue(), limit.get());
			}
		});

	}

	// Initialize Table and Pagination
	@SuppressWarnings("unchecked")
	public void initTablePagination() {

		table = new TableView<>();
		idCol = new TableColumn<>("#");
		nameCol = new TableColumn<>("Name");
		descCol = new TableColumn<>("Description");

		paginationPane = new StackPane();
		pagination = new Pagination();

		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		table.setFixedCellSize(Settings.CELL_SIZE);
		table.setMinHeight(Settings.CELL_SIZE * (limit.get() + 1.01));
		table.setPrefHeight(Settings.CELL_SIZE * (limit.get() + 1.01));

		paginationPane.getChildren().add(pagination);
		pagination.setPageCount(1);
		tableWrapper.getChildren().addAll(table, paginationPane);

		// Set Table Columns
		idCol.setPrefWidth(50);
		idCol.setMinWidth(50);
		idCol.setResizable(true);
		idCol.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Category, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(CellDataFeatures<Category, String> param) {
						String id = Integer.toString(param.getValue().getId());
						return new SimpleStringProperty(id);
					}
				});

		nameCol.setPrefWidth(180);
		nameCol.setMinWidth(180);
		nameCol.setResizable(true);
		nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

		descCol.setPrefWidth(350);
		descCol.setMinWidth(350);
		descCol.setResizable(true);
		descCol.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());

		table.getColumns().addAll(idCol, nameCol, descCol);
		table.setMinWidth(600);

	}

	// Populate Customer Table
	public void populateTable() {
		Task<ArrayList<Category>> task = new Task<ArrayList<Category>>() {

			@Override
			protected ArrayList<Category> call() throws Exception {
				return categoryDAO.getCategories();
			}
		};

		task.setOnSucceeded(e -> {
			if (task.getValue() != null) {
				categories.clear();
				for (Category category : task.getValue()) {
					categories.add(category);
				}
				pagination.setPageCount((int) (Math.ceil(categories.size() * 1.0 / limit.get())));
				pagination.setCurrentPageIndex(0);
				setTablePagination(0, limit.get());
			}

		});

		executor.execute(task);
	}

	// Set current Table page to current index and set limit
	public static void setTablePagination(int index, int limit) {
		int newIndex = index * limit;

		List<Category> categoriesSublist = categories.subList(Math.min(newIndex, categories.size()),
				Math.min(categories.size(), newIndex + limit));
		table.getItems().clear();
		table.setItems(null);
		ObservableList<Category> f = FXCollections.observableArrayList();
		table.setItems(f);
		for (Category category : categoriesSublist) {
			f.add(category);
		}
	}

	// Refresh Table
	public static void refreshTable(int pageNo) {
		pagination.setPageCount((int) (Math.ceil(categories.size() * 1.0 / limit.get())));
		if (pageNo < 0) {
			setTablePagination(pagination.getCurrentPageIndex(), limit.get());
		} else {
			pagination.setCurrentPageIndex(pageNo);
			setTablePagination(pageNo, limit.get());
		}
	}

	// Add new item to Observable List
	public static void addItem(Category category) {
		categories.add(0, category);
		refreshTable(0);
	}

	// Update items in Observable List
	public static void updateItem(Category category, int index) {
		categories.set(index, category);
		refreshTable(-1);
	}

	@FXML
	public void addAction() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/restaurant/views/Category-Add.fxml"));
		try {
			Pane root = loader.load();
			Stage window = new Stage();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/restaurant/application.css").toExternalForm());
			window.setScene(scene);
			window.setTitle("Add Category");
			window.initModality(Modality.APPLICATION_MODAL);
			window.getIcons().add(new Image("/restaurant/images/icon_add.png"));
			window.setResizable(false);
			window.showAndWait();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void deleteAction() throws IOException {
		ObservableList<Category> selectedCategories = table.getSelectionModel().getSelectedItems();
		if (!selectedCategories.isEmpty()) {

			StringJoiner catDelete = new StringJoiner(", ");

			for (Category category : selectedCategories) {
				catDelete.add(category.getName());
			}

			// Delete permission denied for Employee
			if (ScreenManager.getCurrentUser().getRole() != 1) {
				ph.showError(Message.PERMISSION_DENIED);

			} else {

				if (ph.showConfirm(Message.DELETE_WARNING + "Item's Category(s): " + catDelete.toString() + " ?")) {

					// Loop through selected users
					for (Category category : selectedCategories) {
						Task<Boolean> task = new Task<Boolean>() {

							@Override
							protected Boolean call() throws Exception {
								return categoryDAO.delete(category.getId());
							}
						};

						task.setOnSucceeded(e -> {
							if (task.getValue()) {
								categories.remove(category);
								refreshTable(-1);
							} else {
								ph.showError(Message.ERROR);
							}

						});

						executor.execute(task);
					}
				}
			}

		} else {
			ph.showError(Message.ITEM_SELECT_ERROR);
		}
	}

	@FXML
	public void editAction() {

		ObservableList<Category> selectedCategories = table.getSelectionModel().getSelectedItems();
		if (!selectedCategories.isEmpty()) {

			int i = 0;

			for (Category category : selectedCategories) {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/restaurant/views/Category-Edit.fxml"));

				try {
					Pane root = loader.load();
					Stage window = new Stage();
					Scene scene = new Scene(root);

					// Pass selected user and position/index in ObservableList
					// // to //
					CategoryEditController categoryEditController = loader.<CategoryEditController> getController();
					int index = table.getSelectionModel().getSelectedIndices().get(i)
							+ (pagination.getCurrentPageIndex() * limit.get());
					categoryEditController.populateCategoryData(category, index);

					scene.getStylesheets().add(getClass().getResource("/restaurant/application.css").toExternalForm());
					window.setScene(scene);
					window.setTitle("Edit Category");
					window.initModality(Modality.APPLICATION_MODAL);
					window.getIcons().add(new Image("/restaurant/images/icon_edit.png"));
					window.setResizable(false);
					window.show();
					// window.showAndWait();
				} catch (IOException e) {
					e.printStackTrace();
				}
				i++;
			}

		} else {
			ph.showError(Message.ITEM_SELECT_ERROR);
		}

	}

	@FXML
	public void refreshAction() {
		populateTable();
	}

}
