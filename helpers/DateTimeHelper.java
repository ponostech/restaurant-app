package restaurant.helpers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeHelper {

	public static String formattedDateTime(String dateTime) {
		LocalDateTime ld = null;
		try {
			DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			ld = LocalDateTime.parse(dateTime.replace(".0", ""), df);
			DateTimeFormatter df1 = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
			return ld.format(df1);
		} catch (Exception e) {

		}
		return null;
	}

	public static LocalDate formattedDate(String dateTime) {
		LocalDate ld = null;
		try {
			ld = LocalDate.parse(dateTime.replace(".0", ""), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
			return ld;
			// return ld.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ld;
	}
}
