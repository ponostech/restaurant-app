package restaurant.helpers;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class Settings {

	public static final Map<String, String> defaultSettings;

	static {
		defaultSettings = new HashMap<String, String>() {
			{
				put("name", APP_NAME);
				put("address1", null);
				put("address2", null);
				put("name", APP_NAME);
				put("mobile_no", null);
				put("store_no", null);
				put("no_of_tables", NO_OF_TABLES + "");
				put("tax", TAX + "");
				put("service_charge", SERVICE_CHARGE + "");
				put("packing_charge", PACKING_CHARGE + "");
			}
		};
	}

	public static final String APP_NAME = "Restaurant Name";

	public static final int NO_OF_TABLES = 6;

	public static final float TAX = 0.00f;

	public static final float SERVICE_CHARGE = 0.00f;

	public static final float PACKING_CHARGE = 0.00f;

	public static final int CELL_SIZE = 32;

	public static final int ROWS_PER_PAGE_1 = 10;

	public static final int ROWS_PER_PAGE_2 = 15;

}
