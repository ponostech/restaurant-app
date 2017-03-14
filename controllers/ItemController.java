package restaurant.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;

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
import restaurant.dao.ItemDAO;
import restaurant.dao.UserDAO;
import restaurant.helpers.Message;
import restaurant.helpers.PopupHelper;
import restaurant.helpers.ScreenManager;
import restaurant.helpers.Settings;
import restaurant.models.Category;
import restaurant.models.Item;
import restaurant.models.User;

public class ItemController implements Initializable {

	@FXML
	private VBox tableWrapper;

	@FXML
	private JFXTextField searchText;

	@FXML
	private JFXComboBox<String> categoryFilterSelect;

	@FXML
	private Button addButton;

	@FXML
	private Button editButton;

	@FXML
	private Button deleteButton;

	@FXML
	private Button viewButton;

	@FXML
	private Button refreshButton;

	// User Table
	private static TableView<Item> table;
	private static TableColumn<Item, String> nameCol;
	private static TableColumn<Item, String> priceCol;
	private static TableColumn<Item, String> categoryCol;
	private static TableColumn<Item, String> keyCol;
	private static TableColumn<Item, String> userCol;

	private static StackPane paginationPane;
	private static Pagination pagination;
	private static ObservableList<Item> items = FXCollections.observableArrayList();
	private static IntegerProperty limit = new SimpleIntegerProperty(Settings.ROWS_PER_PAGE_1);

	private ItemDAO itemDAO = new ItemDAO();
	private UserDAO userDAO = new UserDAO();
	private CategoryDAO categoryDAO = new CategoryDAO();
	private ArrayList<Category> categories = new ArrayList<>();
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

		categories = categoryDAO.getCategories();
		categories.add(0, new Category(0, "All Categories", ""));

		for (Category category : categories) {
			categoryFilterSelect.getItems().add(category.getName());
		}

		// Initialize Table and Pagination
		initTablePagination();
		populateTable();

		// Bind Table Columns with KeyPress Action Event
		table.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.V) {
				// viewAction();
			}

			if (e.getCode() == KeyCode.E) {
				editAction();
			}

			if (e.getCode() == KeyCode.DELETE) {
				try {
					deleteAction();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		table.setOnMouseClicked(e -> {

			if (e.getButton().equals(MouseButton.PRIMARY)) {
				if (e.getClickCount() == 2) {
					// viewAction();
				}
			}

			if (e.getButton().equals(MouseButton.SECONDARY)) {
				final ContextMenu menu = new ContextMenu();
				MenuItem edit = new MenuItem("Edit Item");
				// MenuItem view = new MenuItem("View Item");
				MenuItem delete = new MenuItem("Delete Item");

				/*
				 * view.setOnAction(e1 -> { viewAction(); });
				 */

				edit.setOnAction(e2 -> {
					editAction();
				});

				delete.setOnAction(e3 -> {
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

		// Search Action
		searchText.setOnKeyPressed(e -> {

			categoryFilterSelect.getSelectionModel().select(0);

			if (searchText.getText().length() >= 2) {

				Task<ArrayList<Item>> task = new Task<ArrayList<Item>>() {

					@Override
					protected ArrayList<Item> call() throws Exception {
						return itemDAO.search(searchText.getText().trim());
					}
				};

				task.setOnSucceeded(e1 -> {
					items.clear();
					for (Item item : task.getValue()) {
						items.add(item);
					}

					int pageCount = (int) (Math.ceil(items.size() * 1.0 / limit.get()));
					pagination.setPageCount(pageCount < 1 ? 1 : pageCount);
					pagination.setCurrentPageIndex(0);
					setTablePagination(0, limit.get());
				});

				executor.execute(task);

			} else {
				refreshAction();
			}

		});

		// Search By Category Action
		categoryFilterSelect.setOnAction(e -> {

			int selectedIndex = categoryFilterSelect.getSelectionModel().getSelectedIndex();
			Category category = categories.get(selectedIndex);
			if (category.getId() != 0) {

				Task<ArrayList<Item>> task = new Task<ArrayList<Item>>() {

					@Override
					protected ArrayList<Item> call() throws Exception {
						return itemDAO.searchByCategory(category.getId());
					}
				};

				task.setOnSucceeded(e1 -> {
					items.clear();
					for (Item item : task.getValue()) {
						items.add(item);
					}

					int pageCount = (int) (Math.ceil(items.size() * 1.0 / limit.get()));
					pagination.setPageCount(pageCount < 1 ? 1 : pageCount);
					pagination.setCurrentPageIndex(0);
					setTablePagination(0, limit.get());
				});

				executor.execute(task);

			} else {
				refreshAction();
			}

		});
	}

	// Initialize Table and Pagination
	@SuppressWarnings("unchecked")
	public void initTablePagination() {

		table = new TableView<>();
		nameCol = new TableColumn<>("Name");
		priceCol = new TableColumn<>("Price");
		keyCol = new TableColumn<>("Shortcut Key");
		categoryCol = new TableColumn<>("Category");
		userCol = new TableColumn<>("Added by");
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

		nameCol.setPrefWidth(200);
		nameCol.setMinWidth(200);
		nameCol.setResizable(true);
		nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

		priceCol.setPrefWidth(100);
		priceCol.setMinWidth(100);
		priceCol.setResizable(true);
		priceCol.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Item, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<Item, String> param) {
						String price = Float.toString(param.getValue().getPrice());
						return new SimpleStringProperty(price);
					}
				});

		categoryCol.setPrefWidth(120);
		categoryCol.setMinWidth(120);
		categoryCol.setResizable(true);
		categoryCol.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Item, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(CellDataFeatures<Item, String> param) {
						Category category = categoryDAO.get(param.getValue().getCategoryId());
						if (category != null)
							return new SimpleStringProperty(category.getName());
						else
							return new SimpleStringProperty("-");
					}
				});

		keyCol.setPrefWidth(110);
		keyCol.setMinWidth(110);
		keyCol.setResizable(true);
		keyCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Item, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Item, String> param) {
				String key = Integer.toString(param.getValue().getShortCutKey());
				return new SimpleStringProperty(key);
			}
		});

		userCol.setPrefWidth(100);
		userCol.setMinWidth(100);
		userCol.setResizable(true);
		userCol.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Item, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(CellDataFeatures<Item, String> param) {
						User user = userDAO.get(param.getValue().getUserId());
						if (user != null)
							return new SimpleStringProperty(user.getUsername());
						else
							return new SimpleStringProperty("-");
					}
				});

		table.getColumns().addAll(nameCol, priceCol, keyCol, categoryCol, userCol);
		table.setMinWidth(650);

	}

	// Populate Users Table
	public void populateTable() {
		Task<ArrayList<Item>> task = new Task<ArrayList<Item>>() {

			@Override
			protected ArrayList<Item> call() throws Exception {
				return itemDAO.getItems();
			}
		};

		task.setOnSucceeded(e -> {
			if (task.getValue() != null) {
				items.clear();
				for (Item item : task.getValue()) {
					items.add(item);
				}
				pagination.setPageCount((int) (Math.ceil(items.size() * 1.0 / limit.get())));
				pagination.setCurrentPageIndex(0);
				setTablePagination(0, limit.get());
			}

		});

		executor.execute(task);
	}

	// Set current Table page to current index and set limit
	public static void setTablePagination(int index, int limit) {
		int newIndex = index * limit;

		List<Item> itemSublist = items.subList(Math.min(newIndex, items.size()),
				Math.min(items.size(), newIndex + limit));
		table.getItems().clear();
		table.setItems(null);
		ObservableList<Item> f = FXCollections.observableArrayList();
		table.setItems(f);
		for (Item item : itemSublist) {
			f.add(item);
		}
	}

	// Refresh Table
	public static void refreshTable(int pageNo) {
		pagination.setPageCount((int) (Math.ceil(items.size() * 1.0 / limit.get())));
		if (pageNo < 0) {
			setTablePagination(pagination.getCurrentPageIndex(), limit.get());
		} else {
			pagination.setCurrentPageIndex(pageNo);
			setTablePagination(pageNo, limit.get());
		}
	}

	// Add new item to Observable List
	public static void addItem(Item item) {
		items.add(0, item);
		refreshTable(0);
	}

	// Update items in Observable List
	public static void updateItem(Item item, int index) {
		items.set(index, item);
		refreshTable(-1);
	}

	@FXML
	public void addAction() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/restaurant/views/Item-Add.fxml"));
		try {
			Pane root = loader.load();
			Stage window = new Stage();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/restaurant/application.css").toExternalForm());
			window.setScene(scene);
			window.setTitle("Add New Item");
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
		ObservableList<Item> selectedItems = table.getSelectionModel().getSelectedItems();
		if (!selectedItems.isEmpty()) {

			StringJoiner itemsDelete = new StringJoiner(", ");

			// Loop through selected users
			for (Item item : selectedItems) {
				itemsDelete.add(item.getName());
			}

			// Delete permission denied for Employee
			if (ScreenManager.getCurrentUser().getRole() != 1) {
				ph.showError(Message.PERMISSION_DENIED);

			} else {

				if (ph.showConfirm(Message.DELETE_WARNING + " Item(s): " + itemsDelete.toString() + " ?")) {

					// Loop through selected users
					for (Item item : selectedItems) {
						Task<Boolean> task = new Task<Boolean>() {

							@Override
							protected Boolean call() throws Exception {
								return itemDAO.delete(item.getId());
							}
						};

						task.setOnSucceeded(e -> {
							if (task.getValue()) {
								items.remove(item);
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
		ObservableList<Item> selectedItems = table.getSelectionModel().getSelectedItems();
		if (!selectedItems.isEmpty()) {
			int i = 0;
			for (Item item : selectedItems) {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/restaurant/views/Item-Edit.fxml"));
				try {
					Pane root = loader.load();
					Stage window = new Stage();
					Scene scene = new Scene(root);

					// Pass selected user and position/index in ObservableList
					// to
					// Edit User Controller
					ItemEditController itemEditController = loader.<ItemEditController> getController();
					int index = table.getSelectionModel().getSelectedIndices().get(i)
							+ (pagination.getCurrentPageIndex() * limit.get());
					itemEditController.populateItemData(item, index);

					scene.getStylesheets().add(getClass().getResource("/restaurant/application.css").toExternalForm());
					window.setScene(scene);
					window.setTitle("Edit Item");
					window.initModality(Modality.APPLICATION_MODAL);
					window.getIcons().add(new Image("/restaurant/images/icon_edit.png"));
					window.setResizable(false);
					window.show();
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
