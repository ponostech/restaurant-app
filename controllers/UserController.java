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
import javafx.beans.property.StringProperty;
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
import restaurant.dao.UserDAO;
import restaurant.helpers.Message;
import restaurant.helpers.PopupHelper;
import restaurant.helpers.ScreenManager;
import restaurant.helpers.Settings;
import restaurant.models.User;

public class UserController implements Initializable {

	@FXML
	private VBox tableWrapper;

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
	private static TableView<User> table;
	private static TableColumn<User, String> usernameCol;
	private static TableColumn<User, String> firstNameCol;
	private static TableColumn<User, String> lastNameCol;
	private static TableColumn<User, String> phoneNoCol;
	private static TableColumn<User, String> roleCol;
	private static StackPane paginationPane;
	private static Pagination pagination;
	private static ObservableList<User> users = FXCollections.observableArrayList();
	private static IntegerProperty limit = new SimpleIntegerProperty(Settings.ROWS_PER_PAGE_1);

	private UserDAO userDAO = new UserDAO();
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
			if (e.getCode() == KeyCode.V) {
				viewAction();
			}

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

			if (e.getButton().equals(MouseButton.PRIMARY)) {
				if (e.getClickCount() == 2) {
					viewAction();
				}
			}

			if (e.getButton().equals(MouseButton.SECONDARY)) {
				final ContextMenu menu = new ContextMenu();
				MenuItem edit = new MenuItem("Edit User");
				MenuItem view = new MenuItem("View User");
				MenuItem delete = new MenuItem("Delete User");

				view.setOnAction(e1 -> {
					viewAction();
				});

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

				menu.getItems().addAll(view, edit, delete);
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
		usernameCol = new TableColumn<>("Username");
		firstNameCol = new TableColumn<>("First Name");
		lastNameCol = new TableColumn<>("Last Name");
		phoneNoCol = new TableColumn<>("Phone No.");
		roleCol = new TableColumn<>("Role");
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
		usernameCol.setPrefWidth(100);
		usernameCol.setMinWidth(100);
		usernameCol.setResizable(true);
		usernameCol.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());

		firstNameCol.setPrefWidth(200);
		firstNameCol.setMinWidth(200);
		firstNameCol.setResizable(true);
		firstNameCol.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());

		lastNameCol.setPrefWidth(100);
		lastNameCol.setMinWidth(100);
		lastNameCol.setResizable(true);
		lastNameCol.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());

		phoneNoCol.setPrefWidth(150);
		phoneNoCol.setMinWidth(150);
		phoneNoCol.setResizable(true);
		phoneNoCol.setCellValueFactory(cellData -> cellData.getValue().phoneNoProperty());

		roleCol.setPrefWidth(150);
		roleCol.setMinWidth(150);
		roleCol.setResizable(true);
		roleCol.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<User, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(CellDataFeatures<User, String> param) {
						StringProperty roleName = new SimpleStringProperty();
						if (param.getValue().getRole() == 1) {
							roleName.set("ADMINISTRATOR");
						} else {
							roleName.set("EMPLOYEE");
						}

						return roleName;
					}
				});

		table.getColumns().addAll(usernameCol, firstNameCol, lastNameCol, phoneNoCol, roleCol);
		table.setMinWidth(710);

	}

	// Populate Users Table
	public void populateTable() {
		Task<ArrayList<User>> task = new Task<ArrayList<User>>() {

			@Override
			protected ArrayList<User> call() throws Exception {
				return userDAO.getUsers();
			}
		};

		task.setOnSucceeded(e -> {
			if (task.getValue() != null) {
				users.clear();
				for (User user : task.getValue()) {
					users.add(user);
				}
				pagination.setPageCount((int) (Math.ceil(users.size() * 1.0 / limit.get())));
				pagination.setCurrentPageIndex(0);
				setTablePagination(0, limit.get());
			}

		});

		executor.execute(task);
	}

	// Set current Table page to current index and set limit
	public static void setTablePagination(int index, int limit) {
		int newIndex = index * limit;

		List<User> userSubList = users.subList(Math.min(newIndex, users.size()),
				Math.min(users.size(), newIndex + limit));
		table.getItems().clear();
		table.setItems(null);
		ObservableList<User> f = FXCollections.observableArrayList();
		table.setItems(f);
		for (User user : userSubList) {
			f.add(user);
		}
	}

	// Refresh Table
	public static void refreshTable(int pageNo) {
		pagination.setPageCount((int) (Math.ceil(users.size() * 1.0 / limit.get())));
		if (pageNo < 0) {
			setTablePagination(pagination.getCurrentPageIndex(), limit.get());
		} else {
			pagination.setCurrentPageIndex(pageNo);
			setTablePagination(pageNo, limit.get());
		}
	}

	// Add new item to Observable List
	public static void addItem(User user) {
		users.add(0, user);
		refreshTable(0);
	}

	// Update items in Observable List
	public static void updateItem(User user, int index) {
		users.set(index, user);
		refreshTable(-1);
	}

	@FXML
	public void addAction() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/restaurant/views/User-Add.fxml"));
		try {
			Pane root = loader.load();
			Stage window = new Stage();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/restaurant/application.css").toExternalForm());
			window.setScene(scene);
			window.setTitle("Add New User");
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
		ObservableList<User> selectedUsers = table.getSelectionModel().getSelectedItems();
		if (!selectedUsers.isEmpty()) {

			StringJoiner usersDelete = new StringJoiner(", ");
			boolean selfDelete = false;

			// Loop through selected users
			for (User user : selectedUsers) {
				usersDelete.add(user.getUsername());
				if (user.getUserId() == ScreenManager.getCurrentUser().getUserId()) {
					selfDelete = true;
					break;
				}
			}

			// Delete permission denied for Employee
			if (ScreenManager.getCurrentUser().getRole() != 1) {
				ph.showError(Message.PERMISSION_DENIED);

			} else if (selfDelete) {
				ph.showError(Message.SELF_DELETE);
			} else {

				if (ph.showConfirm(Message.DELETE_WARNING + "User(s): " + usersDelete.toString() + " ?")) {

					// Loop through selected users
					for (User user : selectedUsers) {
						Task<Boolean> task = new Task<Boolean>() {

							@Override
							protected Boolean call() throws Exception {
								return userDAO.delete(user.getUserId());
							}
						};

						task.setOnSucceeded(e -> {
							if (task.getValue()) {
								users.remove(user);
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
		ObservableList<User> selectedUsers = table.getSelectionModel().getSelectedItems();
		if (!selectedUsers.isEmpty()) {
			int i = 0;
			for (User user : selectedUsers) {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/restaurant/views/User-Edit.fxml"));
				try {
					Pane root = loader.load();
					Stage window = new Stage();
					Scene scene = new Scene(root);

					// Pass selected user and position/index in ObservableList
					// to
					// Edit User Controller
					UserEditController userEditController = loader.<UserEditController> getController();
					int index = table.getSelectionModel().getSelectedIndices().get(i)
							+ (pagination.getCurrentPageIndex() * limit.get());
					userEditController.populateUserData(user, index);

					scene.getStylesheets().add(getClass().getResource("/restaurant/application.css").toExternalForm());
					window.setScene(scene);
					window.setTitle("Edit User");
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

	@FXML
	public void viewAction() {
		ObservableList<User> selectedUsers = table.getSelectionModel().getSelectedItems();
		if (!selectedUsers.isEmpty()) {
			for (User user : selectedUsers) {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/restaurant/views/User-View.fxml"));
				try {
					Pane root = loader.load();
					Stage window = new Stage();
					Scene scene = new Scene(root);

					// Populate User Data
					UserViewController userViewController = loader.<UserViewController> getController();
					userViewController.populateUserData(user);

					scene.getStylesheets().add(getClass().getResource("/restaurant/application.css").toExternalForm());
					window.setScene(scene);
					window.setTitle("User Information");
					window.initModality(Modality.APPLICATION_MODAL);
					window.getIcons().add(new Image("/restaurant/images/icon_info.png"));
					window.setResizable(false);
					window.show();
					// window.showAndWait();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} else {
			ph.showError(Message.ITEM_SELECT_ERROR);
		}
	}

}
