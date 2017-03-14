package restaurant.controllers;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import net.sf.jasperreports.engine.JRException;
import restaurant.PrintReport;
import restaurant.dao.ExpenditureDAO;
import restaurant.dao.InvoiceDAO;
import restaurant.dao.SettingDAO;
import restaurant.helpers.PopupHelper;

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
	private SettingDAO settingDAO = new SettingDAO();

	@FXML
	public void reportAction() {
		
		try {
            // --- Show Jasper Report on click-----
            new PrintReport().showReport();
        } catch (ClassNotFoundException | JRException | SQLException e1) {
            e1.printStackTrace();
        }
		
		/*LocalDate fromDate = fromDateSelect.getValue();
		LocalDate toDate = toDateSelect.getValue();

		if ((fromDate != null) && (toDate != null)) {
			if (fromDate.isAfter(toDate)) {
				ph.showError(Message.DATE_DIFF_ERROR);
			} else {
				Task<ArrayList<Invoice>> task = new Task<ArrayList<Invoice>>() {

					@Override
					protected ArrayList<Invoice> call() throws Exception {
						return invoiceDAO.getInvoices(fromDate.toString(), toDate.toString());
					}
				};

				task.setOnSucceeded(e -> {
					if (task.getValue() != null) {
						for (Invoice invoice : task.getValue()) {

						}

					}

				});

				executor.execute(task);
			}
		} else {
			ph.showError(Message.DATE_SELECT_ERROR);
		}*/
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// Create Executor thread
		executor = Executors.newCachedThreadPool(runnable -> {
			Thread t = new Thread(runnable);
			t.setDaemon(true);
			return t;
		});

		reportTypeSelect.getItems().addAll("Income", "Expenditure");
		reportTypeSelect.setValue("Income");

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
