/**
 * Bank Holiday Cache Class
 * 
 * Once the bank holidays for a given year has been fetched from the internet, they are stored
 * in memory for successive use. The class relies on a HashMap to store the bank holidays.
 *  
 */
package org.stevie.ddsm.diaries.service.bank;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BankHolidayCache {
	
	private BankHolidayCache() {
		
	}

	/*
	 * bank holiday map cache
	 */
	private static Map<Integer, List<BankHoliday>> holidayCache = new HashMap<>();

	/**
	 * Get Bank Holidays From Cache Method
	 * 
	 * Retrieves the bank holidays for a given year from the memory cache. if the year
	 * is not in the cache the method raises an exception.
	 * 
	 * @param year
	 * @return bank holiday collection for specified year
	 */
	public static synchronized List<BankHoliday> getBankHolidaysFromCache(int year) throws CacheFailureException {
		if (!isInCache(year)) {
			throw new CacheFailureException(year);
		}
		return holidayCache.get(year);
	}
	
	/**
	 * Put Bank Holidays Into Cache
	 * 
	 * Store the bank holidays in the cache for specified year
	 * 
	 * @param holidays
	 * @param year
	 * @return none
	 * 
	 */
	public static synchronized void putBankHolidaysInCache(List<BankHoliday> holidays, int year) {
		holidayCache.put(year, holidays);
	}
	
	/**
	 * Check if there is an entry for the given year in the cache. Used in conjunction with 
	 * the get from cache method.
	 * 
	 * @param year
	 * @return true if selected year is already in the cache
	 * 
	 */
	public static synchronized boolean isInCache(int year) {
		return holidayCache.containsKey(year);
	}

	/**
	 * Empty the cache map
	 * 
	 * @return void
	 * 
	 */
	public static synchronized void emptyCache() {
		holidayCache.clear();
	}
	
	/**
	 * Remove Cache Entry
	 * 
	 * Removes the year from the cache map
	 * 
	 * @param year to remove
	 * @return void
	 * 
	 */
	public static synchronized void removeFromCache(int year) {
		if (isInCache(year))
			holidayCache.remove(year);
	}
	
}
