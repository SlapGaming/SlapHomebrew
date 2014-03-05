package me.naithantu.SlapHomebrew.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class DateUtil {

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
	public static String format(String pattern, Date date) {
		if (formatMap.containsKey(pattern)) { //Already contains this pattern
			return formatMap.get(pattern).format(date); //Get format & parse the date
		} else { //Create new format
			SimpleDateFormat format = new SimpleDateFormat(pattern);
			formatMap.put(pattern, format);
			return format.format(date);
		}
	}
	
	/**
	 * Format a date using SimpleDateFormat pattern
	 * @param pattern The pattern
	 * @param date The date
	 * @return The date in string format, by the pattern
	 */
	public static String format(String pattern, long date) {
		return format(pattern, new Date(date));
	}
	
	/**
	 * Format the current time using SimpleDateFormat pattern
	 * @param pattern The pattern
	 * @return The time in string format, by the pattern
	 */
	public static String format(String pattern) {
		return format(pattern, new Date());
	}
	
	/**
	 * Parse a string into a date
	 * @param pattern The Date format
	 * @param date The string
	 * @return The date
	 * @throws ParseException if not a valid date
	 */
	public static Date parse(String pattern, String date) throws ParseException {
		if (formatMap.containsKey(pattern)) { //Already contains this pattern
			return formatMap.get(pattern).parse(date);
		} else { //Create new format
			SimpleDateFormat format = new SimpleDateFormat(pattern);
			formatMap.put(pattern, format);
			return format.parse(date);
		}
	}
	

}
