package restaurant;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.JFrame;

import javafx.concurrent.Task;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.swing.JRViewer;
import restaurant.controllers.SettingsController;
import restaurant.dao.CustomerDAO;
import restaurant.dao.InvoiceDAO;
import restaurant.dao.ItemDAO;
import restaurant.helpers.DateTimeHelper;
import restaurant.models.Customer;
import restaurant.models.Invoice;
import restaurant.models.InvoiceItem;
import restaurant.models.InvoicePrint;

public class PrintInvoice extends JFrame {

	private static final long serialVersionUID = 1L;
	private Executor executor;

	InvoiceDAO invoiceDAO = new InvoiceDAO();
	CustomerDAO customerDAO = new CustomerDAO();
	ItemDAO itemDAO = new ItemDAO();

	public void print(Invoice invoice) throws JRException, ClassNotFoundException, SQLException, FileNotFoundException {

		executor = Executors.newCachedThreadPool(runnable -> {
			Thread t = new Thread(runnable);
			t.setDaemon(true);
			return t;
		});

		InputStream receipt = getClass().getResourceAsStream("/restaurant/views/Invoice.jasper");
		// InputStream input =
		// getClass().getResourceAsStream("/restaurant/views/Receipt.jrxml");

		// First, compile jrxml file.
		// JasperReport receipt = JasperCompileManager.compileReport(input);

		List<InvoicePrint> listItems = new ArrayList<InvoicePrint>();

		/* Map to hold Jasper report Parameters */
		Map<String, Object> parameters = new HashMap<String, Object>();

		Task<Map<String, Object>> task = new Task<Map<String, Object>>() {

			@Override
			protected Map<String, Object> call() throws Exception {
				Map<String, Object> result = new HashMap<>();

				ArrayList<InvoiceItem> invItems = invoiceDAO.getInvoiceItems(invoice.getId());
				Customer customer = customerDAO.get(invoice.getCustomerId());

				result.put("invoice_items", invItems);
				result.put("customer", customer);
				return result;
			}

		};

		task.setOnSucceeded(e -> {
			if (task.getValue() != null) {

				float subTotal = 0f;
				float tax = 0f;
				float serviceCharge = 0f;

				@SuppressWarnings("unchecked")
				ArrayList<InvoiceItem> invItems = (ArrayList<InvoiceItem>) task.getValue().get("invoice_items");
				Customer customer = (Customer) task.getValue().get("customer");

				int slNo = 1;
				for (InvoiceItem invoiceItem : invItems) {
					listItems.add(new InvoicePrint(slNo, itemDAO.get(invoiceItem.getItemId()).getName(),
							String.format("%.02f", invoiceItem.getPrice()), invoiceItem.getQuantity(),
							String.format("%.02f", invoiceItem.getPrice() * invoiceItem.getQuantity())));
					subTotal += invoiceItem.getPrice();
					slNo++;
				}

				/* Convert List to JRBeanCollectionDataSource */
				JRBeanCollectionDataSource itemsJRBean = new JRBeanCollectionDataSource(listItems);

				tax = (invoice.getTax() / 100) * subTotal;
				serviceCharge = (invoice.getServiceCharge() / 100) * subTotal;

				parameters.put("storeName", SettingsController.getSetting("name"));
				parameters.put("address1", SettingsController.getSetting("address1"));
				parameters.put("address2", SettingsController.getSetting("address2"));
				parameters.put("phoneNo", "Ph No:" + SettingsController.getSetting("mobile_no") + "/"
						+ SettingsController.getSetting("store_no"));
				parameters.put("invoiceId", "#" + invoice.getId());
				parameters.put("invoiceDate", DateTimeHelper.formattedDateTime(invoice.getDatetime()));

				if (task.getValue().get("customer") != null) {
					parameters.put("customerName", customer.getFirstName() + " " + customer.getLastName());
				} else {
					parameters.put("customerName", "WALK IN");
				}

				parameters.put("InvoiceData", itemsJRBean);
				parameters.put("subTotal", String.format("%.02f", subTotal));
				parameters.put("taxPercent", String.format("%.02f", invoice.getTax()) + "%");
				parameters.put("taxValue", String.format("%.02f", tax));

				if (invoice.getTableName().contains("TABLE")) {
					parameters.put("serviceChargePercent", String.format("%.02f", invoice.getServiceCharge()) + "%");
					parameters.put("serviceChargeValue", String.format("%.2f", serviceCharge));
				} else {
					parameters.put("serviceChargePercent", "0%");
					parameters.put("serviceChargeValue", "0.00");
				}

				parameters.put("packingCharge", String.format("%.02f", invoice.getPackingChargeAmount()));
				parameters.put("discount", "-" + String.format("%.02f", invoice.getDiscount()));
				parameters.put("total", String.format("%.02f", invoice.getTotal()));
				
				JasperPrint jasperPrint;
				try {
					jasperPrint = JasperFillManager.fillReport(receipt, parameters, new JREmptyDataSource());
					JRViewer viewer = new JRViewer(jasperPrint);
					viewer.setOpaque(true);
					viewer.setVisible(true);
					this.add(viewer);
					this.setSize(550, 800);
					this.setVisible(true);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			} else {

			}
		});

		executor.execute(task);

	}
}
