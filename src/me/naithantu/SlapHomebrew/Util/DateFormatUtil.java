package me.naithantu.SlapHomebrew.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class DateFormatUtil {

	public static HashMap<String, SimpleDateFormat> formatMap;
	
	/**
	 * Initialize the util
	 */
	public static void initialize() {
		formatMap = new HashMap<>();
	}
	
	/**
	 * Destruct the Util
	 */
	public static void destruct() {
		formatMap.clear();
		formatMap = null;
	}
	
	/**
	 * Format a date using SimpleDateFormat pattern
	 * @param pattern The pattern
	 * @param date The date
	 * @return The date in string format, by the pattern
	 */
	public static String formatDate(String pattern, Date date) {
		if (formatMap.containsKey(pattern)) { //Already contains this pattern
			return formatMap.get(pattern).format(date); //Get format & parse the date
		} else { //Create new format
			SimpleDateFormat format = new SimpleDateFormat(pattern);
			formatMap.put(pattern, format);
			return format.format(date);
		}
	}
	
	/**
	 * Format the current time using SimpleDateFormat pattern
	 * @param pattern The pattern
	 * @return The time in string format, by the pattern
	 */
	public static String formatDate(String pattern) {
		return formatDate(pattern, new Date());
	}
	

}
