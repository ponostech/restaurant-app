package restaurant.controllers;

import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.AutoCompletionBinding.ISuggestionRequest;
import org.controlsfx.control.textfield.TextFields;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;

import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import net.sf.jasperreports.engine.JRException;
import restaurant.PrintInvoice;
import restaurant.dao.CustomerDAO;
import restaurant.dao.InvoiceDAO;
import restaurant.dao.ItemDAO;
import restaurant.helpers.Message;
import restaurant.helpers.PopupHelper;
import restaurant.helpers.ScreenManager;
import restaurant.models.Customer;
import restaurant.models.Invoice;
import restaurant.models.InvoiceItem;
import restaurant.models.Item;

public class InvoiceEditController implements Initializable {

	@FXML
	private Label tableNoLabel;

	@FXML
	private JFXTextField searchItemText;

	@FXML
	private TableView<InvoiceItem> invoiceTable;

	@FXML
	private TableColumn<InvoiceItem, String> nameCol;

	@FXML
	private TableColumn<InvoiceItem, Float> priceCol;

	@FXML
	private TableColumn<InvoiceItem, Integer> qtyCol;

	@FXML
	private TableColumn<InvoiceItem, Float> totalCol;

	@FXML
	private TableColumn<InvoiceItem, Button> actionCol;

	@FXML
	private Label subTotalLabel;

	@FXML
	private Label taxLabel;

	@FXML
	private Label serviceChargeLabel;

	@FXML
	private Label packingChargeLabel;

	@FXML
	private JFXTextField discountText;

	@FXML
	private Label totalLabel;

	@FXML
	private JFXComboBox<String> customerSelect;

	@FXML
	private JFXDatePicker dateSelect;

	@FXML
	private JFXButton cancelButton;

	private Customer selectedCustomer = null;
	private ArrayList<Customer> customers = new ArrayList<>();
	private CustomerDAO customerDAO = new CustomerDAO();
	private ItemDAO itemDAO = new ItemDAO();
	private InvoiceDAO invoiceDAO = new InvoiceDAO();

	private ObservableList<InvoiceItem> invoiceItems = FXCollections.observableArrayList();

	private Executor executor;
	private PopupHelper ph = new PopupHelper();

	private String dateFormat = "dd/MM/yyyy";
	private StringConverter<LocalDate> converter;

	private Invoice oldInvoice = null;
	private int tableIndex = 0;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		executor = Executors.newCachedThreadPool(runnable -> {
			Thread t = new Thread(runnable);
			t.setDaemon(true);
			return t;
		});

		// Populate Customers
		customers = customerDAO.getCustomers();
		customers.add(0, new Customer(0l, "WALK IN", "", "", "", ""));

		for (Customer customer : customers) {
			String name = customer.getFirstName() + " " + customer.getLastName();
			if (!customer.getAddress().isEmpty())
				name = name + ", " + customer.getAddress();

			customerSelect.getItems().add(name);
		}

		customerSelect.getSelectionModel().select(0);
		customerSelect.setOnAction(e -> {
			int index = customerSelect.getSelectionModel().getSelectedIndex();
			selectedCustomer = customers.get(index);
		});

		// Delete key event on Table Items
		invoiceTable.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.DELETE) {
				invoiceItems.remove(invoiceTable.getSelectionModel().getSelectedItem());
				calculateTotal();
			}
		});

		AutoCompletionBinding<Item> autoCompletionBinding = TextFields.bindAutoCompletion(searchItemText,
				new Callback<AutoCompletionBinding.ISuggestionRequest, Collection<Item>>() {

					@Override
					public Collection<Item> call(ISuggestionRequest param) {
						try {
							Integer sKey = Integer.parseInt(param.getUserText());
							return itemDAO.searchByShortKey(sKey);
						} catch (NumberFormatException e) {
							return itemDAO.search(param.getUserText());
						}

					}
				}, new StringConverter<Item>() {

					@Override
					public Item fromString(String string) {
						return null;
					}

					@Override
					public String toString(Item object) {
						return object.getName();
					}
				});

		autoCompletionBinding.setOnAutoCompleted(e -> {
			Item item = e.getCompletion();
			boolean notExist = true;
			for (InvoiceItem invoiceItem : invoiceItems) {
				if (invoiceItem.getItemId() == item.getId()) {
					int qty = invoiceItem.getQuantity() + 1;
					invoiceItem.setQuantity(qty);
					invoiceItem.setTotal(qty * invoiceItem.getPrice());
					notExist = false;
					break;
				}
			}
			if (notExist) {
				invoiceItems.add(new InvoiceItem(0l, item.getId(), 0l, item.getPrice(), 1, item.getPrice(), null));
			}
			searchItemText.clear();
			calculateTotal();
		});

		dateSelect.setValue(LocalDate.now());

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

		dateSelect.setConverter(converter);

		// Bind Invoice Table with selected Items
		invoiceTable.setEditable(true);
		invoiceTable.setItems(invoiceItems);
		invoiceTable.setMinWidth(450);
		initInvoiceTable();

		discountText.setOnKeyPressed(e -> {
			calculateTotal();
		});

	}

	// Initialize Invoice Table
	public void initInvoiceTable() {

		nameCol.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<InvoiceItem, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(CellDataFeatures<InvoiceItem, String> param) {
						Integer itemId = param.getValue().getItemId();
						return new SimpleStringProperty(itemDAO.get(itemId).getName());
					}
				});
		priceCol.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<InvoiceItem, Float>, ObservableValue<Float>>() {

					@Override
					public ObservableValue<Float> call(CellDataFeatures<InvoiceItem, Float> param) {
						Float price = param.getValue().getPrice();
						return new SimpleFloatProperty(price).asObject();
					}
				});

		qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

		qtyCol.setCellFactory(TextFieldTableCell.<InvoiceItem, Integer> forTableColumn(new IntegerStringConverter()));

		qtyCol.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<InvoiceItem, Integer>>() {

			@Override
			public void handle(CellEditEvent<InvoiceItem, Integer> t) {
				InvoiceItem item = t.getTableView().getItems().get(t.getTablePosition().getRow());
				for (InvoiceItem invoiceItem : invoiceItems) {
					if (invoiceItem.getItemId() == item.getItemId()) {
						int qty = t.getNewValue();
						invoiceItem.setQuantity(qty);
						invoiceItem.setTotal(qty * invoiceItem.getPrice());
						calculateTotal();
						break;
					}
				}

			}
		});

		totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));

	}

	@FXML
	void getItemsAction() {

	}

	@FXML
	void saveAction() {
		if (!invoiceItems.isEmpty()) {

			float subTotal = 0f;
			float tax = oldInvoice.getTax();
			float taxAmount = 0f;
			float serviceCharge = oldInvoice.getServiceCharge();
			float serviceChargeAmount = 0f;
			float discount = 0;
			float total = 0f;

			for (InvoiceItem invoiceItem : invoiceItems) {
				subTotal += invoiceItem.getTotal();
			}

			taxAmount = (tax / 100) * subTotal;

			if (oldInvoice.getTableName().equals("ORDER/TAKE OUT")) {
				total = subTotal + taxAmount + oldInvoice.getPackingChargeAmount();

			} else {
				serviceChargeAmount = (serviceCharge / 100) * subTotal;
				total = subTotal + taxAmount + serviceChargeAmount;
			}

			try {
				discount = Float.parseFloat(discountText.getText().trim());
				total -= discount;
			} catch (NumberFormatException e) {
			}

			int index = customerSelect.getSelectionModel().getSelectedIndex();
			selectedCustomer = customers.get(index);
			LocalDate date = dateSelect.getValue();
			LocalTime time = LocalTime.now();
			LocalDateTime dateTime = LocalDateTime.of(date, time);
			String dateTimeString = dateTime.toString();

			Invoice invoice = new Invoice(oldInvoice.getId(), ScreenManager.getCurrentUser().getUserId(),
					selectedCustomer.getId(), oldInvoice.getTax(), oldInvoice.getServiceCharge(), discount,
					oldInvoice.getPackingChargeAmount(), total, oldInvoice.getTableName(), dateTimeString);

			Task<Invoice> task = new Task<Invoice>() {

				@Override
				protected Invoice call() throws Exception {
					return invoiceDAO.update(invoice, invoiceItems);
				}
			};

			task.setOnSucceeded(e -> {
				if (task.getValue().getErrorMessage() == null) {
					ph.showInfo(Message.INVOICE_UPDATE);
					cancelAction();
					/*try {
						new PrintInvoice().showReport(task.getValue().getId());
						// new Test().print();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}*/

					InvoiceController.updateItem(task.getValue(), tableIndex);

				} else {
					ph.showError("Error: " + task.getValue().getErrorMessage());
				}
			});

			executor.execute(task);

		} else {
			ph.showError(Message.ITEM_SELECT_ERROR);
		}
	}

	@FXML
	public void printAction() throws ClassNotFoundException, FileNotFoundException, JRException, SQLException {
		cancelAction();
		new PrintInvoice().print(oldInvoice);		
	}

	@FXML
	public void saveKeyPress(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			saveAction();
		}
	}

	@FXML
	void cancelAction() {
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		stage.close();
	}

	@FXML
	public void cancelKeyPress(KeyEvent event) {
		if (event.getCode() == KeyCode.ENTER) {
			cancelAction();
		}
	}

	// Calculate Invoice Total
	public void calculateTotal() {
		float subTotal = 0f;
		float tax = oldInvoice.getTax();
		float taxAmount = 0f;
		float serviceCharge = oldInvoice.getServiceCharge();
		float serviceChargeAmount = 0f;
		float discount = 0;
		float total = 0f;

		if (!invoiceItems.isEmpty()) {
			for (InvoiceItem invoiceItem : invoiceItems) {
				subTotal += invoiceItem.getTotal();
			}
			taxAmount = (tax / 100) * subTotal;
			serviceChargeAmount = (serviceCharge / 100) * subTotal;

			if (oldInvoice.getTableName().equals("ORDER/TAKE OUT")) {
				total = subTotal + taxAmount + oldInvoice.getPackingChargeAmount();
			} else {
				total = subTotal + taxAmount + serviceChargeAmount;
			}

			try {
				discount = Float.parseFloat(discountText.getText().trim());
				total -= discount;
			} catch (NumberFormatException e) {
			}

			subTotalLabel.setText(String.format("%.02f", subTotal));
			totalLabel.setText(String.format("%.02f", total));
		} else {
			subTotalLabel.setText("0");
			totalLabel.setText("0");
		}

	}

	// Populate Invoice Data
	public void populateInvoiceData(Invoice invoice, int i) {
		oldInvoice = invoice;
		tableIndex = i;

		tableNoLabel.setText(invoice.getTableName());
		taxLabel.setText(String.format("%.02f", invoice.getTax()));
		discountText.setText(String.format("%.02f", invoice.getDiscount()));

		if (invoice.getTableName().equals("ORDER/TAKE OUT")) {
			serviceChargeLabel.setText("0.00");
			packingChargeLabel.setText(String.format("%.02f", invoice.getPackingChargeAmount()));
		} else {
			serviceChargeLabel.setText(String.format("%.02f", invoice.getServiceCharge()));
			packingChargeLabel.setText("0.00");
		}

		for (Customer customer : customers) {
			if (invoice.getCustomerId() == customer.getId()) {
				String name = customer.getFirstName() + " " + customer.getLastName();
				if (!customer.getAddress().isEmpty())
					name = name + ", " + customer.getAddress();
				customerSelect.setValue(name);
				break;
			}
		}

		Task<ArrayList<InvoiceItem>> task = new Task<ArrayList<InvoiceItem>>() {

			@Override
			protected ArrayList<InvoiceItem> call() throws Exception {
				return invoiceDAO.getInvoiceItems(invoice.getId());
			}
		};

		task.setOnSucceeded(e -> {

			if (task.getValue() != null) {
				for (InvoiceItem invoiceItem : task.getValue()) {
					invoiceItems.add(invoiceItem);
				}
				calculateTotal();
			}
		});

		executor.execute(task);

	}

}
