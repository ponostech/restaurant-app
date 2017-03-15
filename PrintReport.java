package restaurant;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.swing.JRViewer;
import restaurant.helpers.PopupHelper;
import restaurant.models.Expenditure;
import restaurant.models.SaleReport;

public class PrintReport extends JFrame {

	private static final long serialVersionUID = 2L;

	private PopupHelper ph = new PopupHelper();

	public void printSales(String storeName, String date, List<SaleReport> listItems, float total)
			throws JRException, ClassNotFoundException, SQLException {

		InputStream receipt = getClass().getResourceAsStream("/restaurant/views/Sales-Report.jasper");

		/* Map to hold Jasper report Parameters */
		Map<String, Object> parameters = new HashMap<String, Object>();

		/* Convert List to JRBeanCollectionDataSource */
		JRBeanCollectionDataSource itemsJRBean = new JRBeanCollectionDataSource(listItems);
		parameters.put("storeName", storeName);
		parameters.put("date", date);
		parameters.put("invoiceData", itemsJRBean);
		parameters.put("total", String.format("%.02f", total));

		JasperPrint jasperPrint;
		try {
			jasperPrint = JasperFillManager.fillReport(receipt, parameters, new JREmptyDataSource());
			JRViewer viewer = new JRViewer(jasperPrint);
			viewer.setOpaque(true);
			viewer.setVisible(true);
			this.add(viewer);
			this.setSize(1024, 800);
			this.setVisible(true);
		} catch (Exception e1) {
			ph.showError(e1.getMessage());
		}

	}

	public void printExpenditures(String storeName, String date, List<Expenditure> listItems, float total)
			throws JRException, ClassNotFoundException, SQLException {

		InputStream receipt = getClass().getResourceAsStream("/restaurant/views/Expenditure-Report.jasper");

		/* Map to hold Jasper report Parameters */
		Map<String, Object> parameters = new HashMap<String, Object>();

		/* Convert List to JRBeanCollectionDataSource */
		JRBeanCollectionDataSource itemsJRBean = new JRBeanCollectionDataSource(listItems);
		parameters.put("storeName", storeName);
		parameters.put("date", date);
		parameters.put("expenditureData", itemsJRBean);
		parameters.put("total", String.format("%.02f", total));

		JasperPrint jasperPrint;
		try {
			jasperPrint = JasperFillManager.fillReport(receipt, parameters, new JREmptyDataSource());
			JRViewer viewer = new JRViewer(jasperPrint);
			viewer.setOpaque(true);
			viewer.setVisible(true);
			this.add(viewer);
			this.setSize(1024, 800);
			this.setVisible(true);
		} catch (Exception e1) {
			ph.showError(e1.getMessage());
		}

	}

}
