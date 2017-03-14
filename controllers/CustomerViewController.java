package restaurant.controllers;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import restaurant.dao.InvoiceDAO;
import restaurant.dao.UserDAO;
import restaurant.helpers.DateTimeHelper;
import restaurant.helpers.Message;
import restaurant.helpers.PopupHelper;
import restaurant.helpers.Settings;
import restaurant.models.Customer;
import restaurant.models.Invoice;
import restaurant.models.User;

public class CustomerViewController implements Initializable {

	@FXML
	private Label firstNameLabel;

	@FXML
	private Label lastNameLabel;

	@FXML
	private Label emailLabel;

	@FXML
	private Label phoneNoLabel;

	@FXML
	private Label addressLabel;

	@FXML
	private VBox tableWrapper;

	@FXML
	private JFXDatePicker fromDateSelect;

	@FXML
	private JFXDatePicker toDateSelect;

	@FXML
	private Button refreshButton;

	@FXML
	private Label totalSaleLabel;

	// User Table
	private static TableView<Invoice> table;
	private static TableColumn<Invoice, Integer> idCol;
	private static TableColumn<Invoice, String> tableNameCol;
	private static TableColumn<Invoice, Float> taxCol;
	private static TableColumn<Invoice, Float> serviceChargeCol;
	private static TableColumn<Invoice, Float> totalCol;
	private static TableColumn<Invoice, String> dateCol;
	private static TableColumn<Invoice, String> userCol;

	private static StackPane paginationPane;
	private static Pagination pagination;
	private static ObservableList<Invoice> items = FXCollections.observableArrayList();
	private static IntegerProperty limit = new SimpleIntegerProperty(Settings.ROWS_PER_PAGE_1);

	private InvoiceDAO invoiceDAO = new InvoiceDAO();
	private UserDAO userDAO = new UserDAO();
	private Customer currentCustomer = null;

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

		// Initialize Table and Pagination
		initTablePagination();
		populateTable();

		// Add Listener to Pagination
		pagination.currentPageIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				setTablePagination(newValue.intValue(), limit.get());
			}
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
		tableNameCol = new TableColumn<>("Table");
		taxCol = new TableColumn<>("Tax");
		serviceChargeCol = new TableColumn<>("Service Charge");
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

		idCol.setPrefWidth(50);
		idCol.setMinWidth(50);
		idCol.setResizable(true);
		idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

		tableNameCol.setPrefWidth(150);
		tableNameCol.setMinWidth(150);
		tableNameCol.setResizable(true);
		tableNameCol.setCellValueFactory(new PropertyValueFactory<>("tableName"));

		taxCol.setPrefWidth(50);
		taxCol.setMinWidth(50);
		taxCol.setResizable(true);
		taxCol.setCellValueFactory(new PropertyValueFactory<>("taxAmount"));

		serviceChargeCol.setPrefWidth(150);
		serviceChargeCol.setMinWidth(150);
		serviceChargeCol.setResizable(true);
		serviceChargeCol.setCellValueFactory(new PropertyValueFactory<>("serviceChargeAmount"));

		totalCol.setPrefWidth(80);
		totalCol.setMinWidth(80);
		totalCol.setResizable(true);
		totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));

		dateCol.setPrefWidth(150);
		dateCol.setMinWidth(150);
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

		table.getColumns().addAll(idCol, tableNameCol, taxCol, serviceChargeCol, totalCol, dateCol, userCol);
		table.setMinWidth(750);

	}

	// Populate Users Table
	public void populateTable() {
		Task<ArrayList<Invoice>> task = new Task<ArrayList<Invoice>>() {

			@Override
			protected ArrayList<Invoice> call() throws Exception {
				return invoiceDAO.getCustomerInvoices(currentCustomer.getId());
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

	@FXML
	public void refreshAction() {
		populateTable();
	}

	@FXML
	public void historyTab() {
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
						return invoiceDAO.getCustomerInvoices(fromDateTime.toString(), toDateTime.toString(),
								currentCustomer.getId());
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

	// Populate Customer Data
	public void populateCustomerData(Customer customer) {
		currentCustomer = customer;
		firstNameLabel.setText(customer.getFirstName());
		lastNameLabel.setText(customer.getLastName());
		emailLabel.setText(customer.getEmail());
		phoneNoLabel.setText(customer.getPhoneNo());
		addressLabel.setText(customer.getAddress());
	}
}
