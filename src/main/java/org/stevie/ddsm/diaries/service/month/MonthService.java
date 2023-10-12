/** 
 * <h3>Month Service Class<h3>
 * 
 * <p>This class simply returns the correct ending for a particular day for display in the duplication 
 * diary dialog.</p>
 * 
 * @author Stephen
 */

package org.stevie.ddsm.diaries.service.month;

import java.util.HashMap;

import org.springframework.stereotype.Component;

@Component
public final class MonthService {
		
	/*
	 * store the endings in a map
	 */
	private static HashMap<Integer, String> monthMap = new HashMap<>();
	
	private MonthService() {
		
	}
	/*
	 * Static constructor
	 * 
	 * Initialise the map with the correct endings for each day in a month
	 * 
	 */
	static {
		monthMap.put(1, "st");
		monthMap.put(2, "nd");
		monthMap.put(3, "rd");
		monthMap.put(4, "th");
		monthMap.put(5, "th");
		monthMap.put(6, "th");
		monthMap.put(7, "th");
		monthMap.put(8, "th");
		monthMap.put(9, "th");
		monthMap.put(10, "th");
		monthMap.put(11, "th");
		monthMap.put(12, "th");
		monthMap.put(13, "th");
		monthMap.put(14, "th");
		monthMap.put(15, "th");
		monthMap.put(16, "th");
		monthMap.put(17, "th");
		monthMap.put(18, "th");
		monthMap.put(19, "th");
		monthMap.put(20, "th");
		monthMap.put(21, "st");
		monthMap.put(22, "nd");
		monthMap.put(23, "rd");
		monthMap.put(24, "th");
		monthMap.put(25, "th");
		monthMap.put(26, "th");
		monthMap.put(27, "th");
		monthMap.put(28, "th");
		monthMap.put(29, "th");
		monthMap.put(30, "th");
		monthMap.put(31, "st");
		
	}
	
	/**
	 * Get Ending Static Method
	 * Retrieve correct ending for a particular day in a month 1-31
	 * @param dayOfTheMonth
	 * @return day ending
	 */
	public static String getEnding(int dayOfTheMonth) {
		/*
		 * check argument
		 */
		if (dayOfTheMonth < 1 || dayOfTheMonth > 31) 
			throw new IllegalArgumentException("day of the month should be in the range 1 to 31");
		
		/*
		 * get ending
		 */
		return monthMap.get(dayOfTheMonth);
	}
}
