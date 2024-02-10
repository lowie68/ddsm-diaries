/**
 * <h3>Bank Holiday Cache Class</h3>
 * 
 * <p>Once the bank holidays for a given year have been fetched from the Internet REST API, 
 * they are stored in memory for successive use. The class relies on a HashMap to store the 
 * bank holidays in memory.</p>
 *  
 *  @author Stephen
 *  @version 1.0
 */
package org.stevie.ddsm.diaries.service.bank;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bank Holiday Cache Class
 * 
 * This class improves performance by storing bank holidays in memory. It contains
 * only static members. Do not create objects of this class.
 * 
 */
public final class BankHolidayCache {
	
	/**
	 * Default Constructor
	 * 
	 * Marked private so no one can create instances of this object
	 *  
	 */
	private BankHolidayCache() {
		
	}

	/*
	 * bank holiday map cache where bank holidays are kept in memory
	 * The key of the cache is the year and the value is a list of bank
	 * holidays for the given year.
	 */
	private static Map<Integer, List<BankHoliday>> holidayCache = new HashMap<>();

	/**
	 * Get Bank Holidays From Cache Method
	 * 
	 * Retrieves the bank holidays for a given year from the memory cache. if the year
	 * is not in the cache the method raises an exception.
	 * 
	 * @param year
	 * @return bank holidays for specified year
	 * @since 1.0
	 * 
	 */
	public static synchronized List<BankHoliday> getBankHolidaysFromCache(int year) throws CacheFailureException {
		if (!isInCache(year)) {
			throw new CacheFailureException(year);
		}
		return holidayCache.get(year);
	}
	
	/**
	 * Put Bank Holidays Into Cache Method
	 * 
	 * Store the bank holidays in the cache for specified year
	 * 
	 * @param bank holidays
	 * @param year
	 * 
	 * @since 1.0
	 * 
	 */
	public static synchronized void putBankHolidaysInCache(List<BankHoliday> holidays, int year) {
		holidayCache.put(year, holidays);
	}
	
	/**
	 * Is In Cache Method
	 * 
	 * Check if there is an entry for the given year in the cache. Used in conjunction with 
	 * the get from cache method.
	 * 
	 * @param year
	 * @return true if selected year is already in the cache
	 * @since 1.0
	 * 
	 */
	public static synchronized boolean isInCache(int year) {
		return holidayCache.containsKey(year);
	}

	/**
	 * Empty Cache Method
	 * 
	 * deletes all years from the cache. Not currently in use.
	 * 
	 * @since 1.0
	 * 
	 */
	public static synchronized void emptyCache() {
		holidayCache.clear();
	}
	
	/**
	 * Remove From Cache Method
	 * 
	 * Removes the year from the cache map. Not currently in use.
	 * 
	 * @param year to remove
	 * @since 1.0
	 * 
	 */
	public static synchronized void removeFromCache(int year) {
		if (isInCache(year))
			holidayCache.remove(year);
	}
	
}
