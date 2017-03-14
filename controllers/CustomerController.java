package restaurant.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.jfoenix.controls.JFXTextField;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import restaurant.dao.CustomerDAO;
import restaurant.helpers.Message;
import restaurant.helpers.PopupHelper;
import restaurant.helpers.ScreenManager;
import restaurant.helpers.Settings;
import restaurant.models.Customer;

public class CustomerController implements Initializable {

	@FXML
	private VBox tableWrapper;

	@FXML
	private JFXTextField searchText;

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
	private static TableView<Customer> table;
	private static TableColumn<Customer, String> firstNameCol;
	private static TableColumn<Customer, String> lastNameCol;
	private static TableColumn<Customer, String> emailCol;
	private static TableColumn<Customer, String> phoneNoCol;
	private static TableColumn<Customer, String> addressCol;
	private static StackPane paginationPane;
	private static Pagination pagination;
	private static ObservableList<Customer> customers = FXCollections.observableArrayList();
	private static IntegerProperty limit = new SimpleIntegerProperty(Settings.ROWS_PER_PAGE_1);

	private CustomerDAO customerDAO = new CustomerDAO();
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
				MenuItem edit = new MenuItem("Edit Customer");
				MenuItem view = new MenuItem("View Customer");
				MenuItem delete = new MenuItem("Delete Customer");

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

		// Search File Action
		searchText.setOnKeyPressed(e -> {

			if (searchText.getText().length() >= 2) {

				Task<ArrayList<Customer>> task = new Task<ArrayList<Customer>>() {

					@Override
					protected ArrayList<Customer> call() throws Exception {
						return customerDAO.search(searchText.getText().trim());
					}
				};

				task.setOnSucceeded(e1 -> {
					customers.clear();
					for (Customer customer : task.getValue()) {
						customers.add(customer);
					}

					int pageCount = (int) (Math.ceil(customers.size() * 1.0 / limit.get()));
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
		firstNameCol = new TableColumn<>("First Name");
		lastNameCol = new TableColumn<>("Last Name");
		emailCol = new TableColumn<>("Email");
		phoneNoCol = new TableColumn<>("Phone No.");
		addressCol = new TableColumn<>("Address");
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

		firstNameCol.setPrefWidth(200);
		firstNameCol.setMinWidth(200);
		firstNameCol.setResizable(true);
		firstNameCol.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());

		lastNameCol.setPrefWidth(100);
		lastNameCol.setMinWidth(100);
		lastNameCol.setResizable(true);
		lastNameCol.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());

		emailCol.setPrefWidth(200);
		emailCol.setMinWidth(200);
		emailCol.setResizable(true);
		emailCol.setCellValueFactory(cellData -> cellData.getValue().emailProperty());

		phoneNoCol.setPrefWidth(100);
		phoneNoCol.setMinWidth(100);
		phoneNoCol.setResizable(true);
		phoneNoCol.setCellValueFactory(cellData -> cellData.getValue().phoneNoProperty());

		addressCol.setPrefWidth(200);
		addressCol.setMinWidth(200);
		addressCol.setResizable(true);
		addressCol.setCellValueFactory(cellData -> cellData.getValue().addressProperty());

		table.getColumns().addAll(firstNameCol, lastNameCol, emailCol, phoneNoCol, addressCol);
		table.setMinWidth(720);

	}

	// Populate Customer Table
	public void populateTable() {
		Task<ArrayList<Customer>> task = new Task<ArrayList<Customer>>() {

			@Override
			protected ArrayList<Customer> call() throws Exception {
				return customerDAO.getCustomers();
			}
		};

		task.setOnSucceeded(e -> {
			if (task.getValue() != null) {
				customers.clear();
				for (Customer customer : task.getValue()) {
					customers.add(customer);
				}
				pagination.setPageCount((int) (Math.ceil(customers.size() * 1.0 / limit.get())));
				pagination.setCurrentPageIndex(0);
				setTablePagination(0, limit.get());
			}

		});

		executor.execute(task);
	}

	// Set current Table page to current index and set limit
	public static void setTablePagination(int index, int limit) {
		int newIndex = index * limit;

		List<Customer> customerSubList = customers.subList(Math.min(newIndex, customers.size()),
				Math.min(customers.size(), newIndex + limit));
		table.getItems().clear();
		table.setItems(null);
		ObservableList<Customer> f = FXCollections.observableArrayList();
		table.setItems(f);
		for (Customer customer : customerSubList) {
			f.add(customer);
		}
	}

	// Refresh Table
	public static void refreshTable(int pageNo) {
		pagination.setPageCount((int) (Math.ceil(customers.size() * 1.0 / limit.get())));
		if (pageNo < 0) {
			setTablePagination(pagination.getCurrentPageIndex(), limit.get());
		} else {
			pagination.setCurrentPageIndex(pageNo);
			setTablePagination(pageNo, limit.get());
		}
	}

	// Add new item to Observable List
	public static void addItem(Customer customer) {
		customers.add(0, customer);
		refreshTable(0);
	}

	// Update items in Observable List
	public static void updateItem(Customer customer, int index) {
		customers.set(index, customer);
		refreshTable(-1);
	}

	@FXML
	public void addAction() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/restaurant/views/Customer-Add.fxml"));
		try {
			Pane root = loader.load();
			Stage window = new Stage();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("/restaurant/application.css").toExternalForm());
			window.setScene(scene);
			window.setTitle("Add New Customer");
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
		ObservableList<Customer> selectedCustomers = table.getSelectionModel().getSelectedItems();
		if (!selectedCustomers.isEmpty()) {

			StringJoiner customersDelete = new StringJoiner(", ");

			for (Customer customer : selectedCustomers) {
				customersDelete.add(customer.getFirstName());
			}

			// Delete permission denied for Employee
			if (ScreenManager.getCurrentUser().getRole() != 1) {
				ph.showError(Message.PERMISSION_DENIED);

			} else {

				if (ph.showConfirm(Message.DELETE_WARNING + "Customer(s): " + customersDelete.toString() + " ?")) {

					// Loop through selected users
					for (Customer customer : selectedCustomers) {
						Task<Boolean> task = new Task<Boolean>() {

							@Override
							protected Boolean call() throws Exception {
								return customerDAO.delete(customer.getId());
							}
						};

						task.setOnSucceeded(e -> {
							if (task.getValue()) {
								customers.remove(customer);
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
		ObservableList<Customer> selectedCustomers = table.getSelectionModel().getSelectedItems();
		if (!selectedCustomers.isEmpty()) {
			int i = 0;
			for (Customer customer : selectedCustomers) {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/restaurant/views/Customer-Edit.fxml"));
				try {
					Pane root = loader.load();
					Stage window = new Stage();
					Scene scene = new Scene(root);

					// Pass selected user and position/index in ObservableList
					// to
					// Edit User Controller
					CustomerEditController customerEditController = loader.<CustomerEditController> getController();
					int index = table.getSelectionModel().getSelectedIndices().get(i)
							+ (pagination.getCurrentPageIndex() * limit.get());
					customerEditController.populateCustomerData(customer, index);

					scene.getStylesheets().add(getClass().getResource("/restaurant/application.css").toExternalForm());
					window.setScene(scene);
					window.setTitle("Edit Customer");
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
		ObservableList<Customer> selectedCustomers = table.getSelectionModel().getSelectedItems();
		if (!selectedCustomers.isEmpty()) {
			for (Customer customer : selectedCustomers) {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/restaurant/views/Customer-Details.fxml"));
				try {
					ScrollPane root = loader.load();
					Stage window = new Stage();
					Scene scene = new Scene(root);

					// Populate User Data
					CustomerViewController customerViewController = loader.<CustomerViewController> getController();
					customerViewController.populateCustomerData(customer);

					scene.getStylesheets().add(getClass().getResource("/restaurant/application.css").toExternalForm());
					window.setScene(scene);
					window.setTitle("Customer Information");
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
