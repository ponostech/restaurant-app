package restaurant.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringJoiner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;

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
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import restaurant.dao.CustomerDAO;
import restaurant.dao.InvoiceDAO;
import restaurant.dao.SettingDAO;
import restaurant.dao.UserDAO;
import restaurant.helpers.DateTimeHelper;
import restaurant.helpers.Message;
import restaurant.helpers.PopupHelper;
import restaurant.helpers.ScreenManager;
import restaurant.helpers.Settings;
import restaurant.models.Customer;
import restaurant.models.Invoice;
import restaurant.models.User;

public class InvoiceController implements Initializable {

	@FXML
	private VBox tableWrapper;

	@FXML
	private JFXDatePicker fromDateSelect;

	@FXML
	private JFXDatePicker toDateSelect;

	@FXML
	private Label totalSaleLabel;

	@FXML
	private JFXComboBox<String> tableFilterSelect;

	@FXML
	private Button editButton;

	@FXML
	private Button deleteButton;

	@FXML
	private Button viewButton;

	@FXML
	private Button refreshButton;

	// User Table
	private static TableView<Invoice> table;
	private static TableColumn<Invoice, Integer> idCol;
	private static TableColumn<Invoice, String> customerCol;
	private static TableColumn<Invoice, String> tableNameCol;
	private static TableColumn<Invoice, Float> totalCol;
	private static TableColumn<Invoice, String> dateCol;
	private static TableColumn<Invoice, String> userCol;

	private static StackPane paginationPane;
	private static Pagination pagination;
	private static ObservableList<Invoice> items = FXCollections.observableArrayList();
	private static IntegerProperty limit = new SimpleIntegerProperty(Settings.ROWS_PER_PAGE_1);

	private InvoiceDAO invoiceDAO = new InvoiceDAO();
	private UserDAO userDAO = new UserDAO();
	private CustomerDAO customerDAO = new CustomerDAO();
	private SettingDAO settingDAO = new SettingDAO();

	private ArrayList<String> tables = new ArrayList<>();
	private Executor executor;
	PopupHelper ph = new PopupHelper();

	private String dateFormat = "dd/MM/yyyy";
	private StringConverter<LocalDate> converter;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// Create Executor thread
		executor = Executors.newCachedThreadPool(runnable -> {
			Thread t = new Thread(runnable);
			t.setDaemon(true);
			return t;
		});

		// Invoice Table Names
		tables = invoiceDAO.getTables();
		if (tables.isEmpty()) {
			int noOfTable = Settings.NO_OF_TABLES;
			if (settingDAO.get("no_of_tables") != null) {
				noOfTable = Integer.parseInt(settingDAO.get("no_of_tables"));
			}
			for (int i = 0; i < noOfTable; i++) {
				tables.add("TABLE " + (i + 1));
			}
		}

		tables.add(0, "All Tables");

		for (String tableName : tables) {
			tableFilterSelect.getItems().add(tableName);
		}

		tableFilterSelect.setValue("All Tables");

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
					e1.printStackTrace();
				}
			}
		});

		table.setOnMouseClicked(e -> {

			if (e.getButton().equals(MouseButton.PRIMARY)) {
				if (e.getClickCount() == 2) {
					editAction();
				}
			}

			if (e.getButton().equals(MouseButton.SECONDARY)) {
				final ContextMenu menu = new ContextMenu();
				MenuItem edit = new MenuItem("Edit Invoice");
				MenuItem delete = new MenuItem("Delete Invoice");

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

		// Search By Category Action
		tableFilterSelect.setOnAction(e -> {

			String tableName = tableFilterSelect.getSelectionModel().getSelectedItem();

			Task<ArrayList<Invoice>> task = new Task<ArrayList<Invoice>>() {

				@Override
				protected ArrayList<Invoice> call() throws Exception {
					return invoiceDAO.searchByTableName(tableName);
				}
			};

			task.setOnSucceeded(e1 -> {
				items.clear();
				float total = 0f;
				for (Invoice invoice : task.getValue()) {
					items.add(invoice);
					total += invoice.getTotal();
				}
				totalSaleLabel.setText(" Rs " + String.format("%.02f", total));

				int pageCount = (int) (Math.ceil(items.size() * 1.0 / limit.get()));
				pagination.setPageCount(pageCount < 1 ? 1 : pageCount);
				pagination.setCurrentPageIndex(0);
				setTablePagination(0, limit.get());
			});

			executor.execute(task);

		});

		converter = new StringConverter<LocalDate>() {

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);

			@Override
			public LocalDate fromString(String string) {
				if (string != null && !string.isEmpty()) {
					return LocalDate.parse(string, formatter);
				} else {
					return null;
				}
			}

			@Override
			public String toString(LocalDate date) {
				if (date != null) {
					return formatter.format(date);
				} else {
					return "";
				}
			}
		};

		fromDateSelect.setConverter(converter);
		toDateSelect.setConverter(converter);
	}

	// Initialize Table and Pagination
	@SuppressWarnings("unchecked")
	public void initTablePagination() {

		table = new TableView<>();
		idCol = new TableColumn<>("#");
		customerCol = new TableColumn<>("Customer Name");
		tableNameCol = new TableColumn<>("Table");
		totalCol = new TableColumn<>("Total");
		dateCol = new TableColumn<>("Date");
		userCol = new TableColumn<>("Added By");

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

		idCol.setPrefWidth(60);
		idCol.setMinWidth(60);
		idCol.setResizable(true);
		idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

		customerCol.setPrefWidth(220);
		customerCol.setMinWidth(220);
		customerCol.setResizable(true);
		customerCol.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Invoice, String>, ObservableValue<String>>() {
					@Override
					public ObservableValue<String> call(CellDataFeatures<Invoice, String> param) {
						Customer customer = customerDAO.get(param.getValue().getCustomerId());
						if (customer == null) {
							return new SimpleStringProperty("WALK IN");
						} else {
							return new SimpleStringProperty(customer.getFirstName() + " " + customer.getLastName());
						}

					}
				});

		tableNameCol.setPrefWidth(150);
		tableNameCol.setMinWidth(150);
		tableNameCol.setResizable(true);
		tableNameCol.setCellValueFactory(new PropertyValueFactory<>("tableName"));

		totalCol.setPrefWidth(100);
		totalCol.setMinWidth(100);
		totalCol.setResizable(true);
		totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));

		dateCol.setPrefWidth(180);
		dateCol.setMinWidth(180);
		dateCol.setResizable(true);
		dateCol.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Invoice, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(CellDataFeatures<Invoice, String> param) {
						String date = DateTimeHelper.formattedDateTime(param.getValue().getDatetime());
						StringProperty formattedDate = new SimpleStringProperty(date);
						return formattedDate;
					}
				});

		userCol.setPrefWidth(100);
		userCol.setMinWidth(100);
		userCol.setResizable(true);
		userCol.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<Invoice, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(CellDataFeatures<Invoice, String> param) {
						User user = userDAO.get(param.getValue().getUserId());
						if (user != null)
							return new SimpleStringProperty(user.getUsername());
						else
							return new SimpleStringProperty("-");
					}
				});

		table.getColumns().addAll(idCol, customerCol, tableNameCol, totalCol, dateCol, userCol);
		table.setMinWidth(820);

	}

	// Populate Users Table
	public void populateTable() {
		Task<ArrayList<Invoice>> task = new Task<ArrayList<Invoice>>() {

			@Override
			protected ArrayList<Invoice> call() throws Exception {
				return invoiceDAO.getInvoices();
			}
		};

		task.setOnSucceeded(e -> {
			if (task.getValue() != null) {
				float total = 0f;
				items.clear();
				for (Invoice invoice : task.getValue()) {
					items.add(invoice);
					total += invoice.getTotal();
				}
				totalSaleLabel.setText(" Rs " + String.format("%.02f", total));
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

		List<Invoice> invoiceSublist = items.subList(Math.min(newIndex, items.size()),
				Math.min(items.size(), newIndex + limit));
		table.getItems().clear();
		table.setItems(null);
		ObservableList<Invoice> f = FXCollections.observableArrayList();
		table.setItems(f);
		for (Invoice item : invoiceSublist) {
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

	// Update items in Observable List
	public static void updateItem(Invoice invoice, int index) {
		items.set(index, invoice);
		refreshTable(-1);
	}

	@FXML
	public void deleteAction() throws IOException {
		ObservableList<Invoice> selectedItems = table.getSelectionModel().getSelectedItems();
		if (!selectedItems.isEmpty()) {

			StringJoiner itemsDelete = new StringJoiner(", ");

			// Loop through selected items
			for (Invoice item : selectedItems) {
				itemsDelete.add("#" + item.getId());
			}

			// Delete permission denied for Employee
			if (ScreenManager.getCurrentUser().getRole() != 1) {
				ph.showError(Message.PERMISSION_DENIED);

			} else {

				if (ph.showConfirm(Message.DELETE_WARNING + " Invoice(s): " + itemsDelete.toString() + " ?")) {

					// Loop through selected users
					for (Invoice invoice : selectedItems) {
						Task<Boolean> task = new Task<Boolean>() {

							@Override
							protected Boolean call() throws Exception {
								return invoiceDAO.delete(invoice.getId());
							}
						};

						task.setOnSucceeded(e -> {
							if (task.getValue()) {
								items.remove(invoice);
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
		ObservableList<Invoice> selectedItems = table.getSelectionModel().getSelectedItems();
		if (!selectedItems.isEmpty()) {
			int i = 0;
			for (Invoice item : selectedItems) {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("/restaurant/views/Invoice-Edit.fxml"));
				try {
					ScrollPane root = loader.load();
					Stage window = new Stage();
					Scene scene = new Scene(root);

					// Pass selected user and position/index in ObservableList
					// to
					// Edit User Controller
					InvoiceEditController invoiceEditController = loader.<InvoiceEditController> getController();
					int index = table.getSelectionModel().getSelectedIndices().get(i)
							+ (pagination.getCurrentPageIndex() * limit.get());
					invoiceEditController.populateInvoiceData(item, index);

					scene.getStylesheets().add(getClass().getResource("/restaurant/application.css").toExternalForm());
					window.setScene(scene);
					window.setTitle("Edit Invoice");
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

	@FXML
	public void searchByDateAction() {
		LocalDateTime fromDateTime = LocalDateTime.of(fromDateSelect.getValue(), LocalTime.MIDNIGHT);
		LocalDateTime toDateTime = LocalDateTime.of(toDateSelect.getValue(), LocalTime.MAX);

		if ((fromDateTime != null) && (toDateTime != null)) {
			if (fromDateTime.isAfter(toDateTime)) {
				ph.showError(Message.DATE_DIFF_ERROR);
			} else {
				Task<ArrayList<Invoice>> task = new Task<ArrayList<Invoice>>() {

					@Override
					protected ArrayList<Invoice> call() throws Exception {
						return invoiceDAO.getInvoices(fromDateTime.toString(), toDateTime.toString());
					}
				};

				task.setOnSucceeded(e -> {
					if (task.getValue() != null) {
						float total = 0f;
						items.clear();
						for (Invoice invoice : task.getValue()) {
							items.add(invoice);
							total += invoice.getTotal();
						}
						totalSaleLabel.setText(" Rs " + String.format("%.02f", total));
						pagination.setPageCount((int) (Math.ceil(items.size() * 1.0 / limit.get())));
						pagination.setCurrentPageIndex(0);
						setTablePagination(0, limit.get());
					}

				});

				executor.execute(task);
			}
		} else {
			ph.showError(Message.DATE_SELECT_ERROR);
		}

	}

}
