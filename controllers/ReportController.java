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

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import restaurant.PrintReport;
import restaurant.dao.CustomerDAO;
import restaurant.dao.ExpenditureDAO;
import restaurant.dao.InvoiceDAO;
import restaurant.dao.SettingDAO;
import restaurant.helpers.DateTimeHelper;
import restaurant.helpers.Message;
import restaurant.helpers.PopupHelper;
import restaurant.models.Customer;
import restaurant.models.Expenditure;
import restaurant.models.Invoice;
import restaurant.models.SaleReport;

public class ReportController implements Initializable {
	@FXML
	private VBox tableWrapper;

	@FXML
	private JFXDatePicker fromDateSelect;

	@FXML
	private JFXDatePicker toDateSelect;

	@FXML
	private JFXComboBox<String> reportTypeSelect;

	private String dateFormat = "dd/MM/yyyy";
	private StringConverter<LocalDate> converter;
	private PopupHelper ph = new PopupHelper();
	private Executor executor;
	private InvoiceDAO invoiceDAO = new InvoiceDAO();
	private ExpenditureDAO expenditureDAO = new ExpenditureDAO();
	private CustomerDAO customerDAO = new CustomerDAO();
	private SettingDAO settingDAO = new SettingDAO();

	@FXML
	public void reportAction() {

		LocalDateTime fromDateTime;
		LocalDateTime toDateTime;

		try {
			fromDateTime = LocalDateTime.of(fromDateSelect.getValue(), LocalTime.MIDNIGHT);
			toDateTime = LocalDateTime.of(toDateSelect.getValue(), LocalTime.MAX);

			if ((fromDateTime != null) && (toDateTime != null)) {
				if (fromDateTime.isAfter(toDateTime)) {
					ph.showError(Message.DATE_DIFF_ERROR);
				} else {

					String date = "From "
							+ DateTimeHelper.formattedDateTime(
									fromDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
							+ " to " + DateTimeHelper.formattedDateTime(
									toDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

					// Sales Report Type
					if (reportTypeSelect.getSelectionModel().getSelectedIndex() == 0) {
						Task<ArrayList<Invoice>> task = new Task<ArrayList<Invoice>>() {

							@Override
							protected ArrayList<Invoice> call() throws Exception {
								return invoiceDAO.getInvoices(fromDateTime.toString(), toDateTime.toString());
							}
						};

						task.setOnSucceeded(e -> {
							if (!task.getValue().isEmpty()) {

								List<SaleReport> saleItems = new ArrayList<SaleReport>();

								float total = 0f;
								for (Invoice invoice : task.getValue()) {
									String customerName = "";
									Customer customer = customerDAO.get(invoice.getCustomerId());
									if (customer != null) {
										customerName = customer.getFirstName() + " " + customer.getLastName();
									} else {
										customerName = "WALK IN";
									}

									saleItems.add(new SaleReport(invoice.getId(),
											DateTimeHelper.formattedDateTime(invoice.getDatetime()), customerName,
											invoice.getTableName(), String.format("%.02f", invoice.getTotal())));
									total += invoice.getTotal();
								}

								try {
									new PrintReport().printSales(settingDAO.get("name"), date, saleItems, total);
								} catch (Exception e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}

							} else {
								ph.showError(Message.NO_DATA);
							}

						});

						executor.execute(task);
					} else {
						Task<ArrayList<Expenditure>> task = new Task<ArrayList<Expenditure>>() {

							@Override
							protected ArrayList<Expenditure> call() throws Exception {
								return expenditureDAO.getExpenditures(fromDateTime.toString(), toDateTime.toString());
							}
						};

						task.setOnSucceeded(e -> {
							if (!task.getValue().isEmpty()) {

								List<Expenditure> expItems = new ArrayList<Expenditure>();

								float total = 0f;
								long ctr = 1;
								for (Expenditure expenditure : task.getValue()) {

									expItems.add(new Expenditure(ctr, 0, expenditure.getDescription(),
											expenditure.getAmount(),
											DateTimeHelper.formattedDateTime(expenditure.getDatetime())));
									total += expenditure.getAmount();
									ctr++;
								}

								try {
									new PrintReport().printExpenditures(settingDAO.get("name"), date, expItems, total);
								} catch (Exception e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}

							} else {
								ph.showError(Message.NO_DATA);
							}

						});

						executor.execute(task);
					}

				}
			} else {
				ph.showError(Message.DATE_SELECT_ERROR);
			}

		} catch (Exception e2) {
			ph.showError(Message.DATE_SELECT_ERROR);
		}

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// Create Executor thread
		executor = Executors.newCachedThreadPool(runnable -> {
			Thread t = new Thread(runnable);
			t.setDaemon(true);
			return t;
		});

		reportTypeSelect.getItems().addAll("Sales Report", "Expenditures Report");
		reportTypeSelect.setValue("Sales Report");

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
}
