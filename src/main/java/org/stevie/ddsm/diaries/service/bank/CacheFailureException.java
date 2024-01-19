/**
 * Cache Failure Exception Class
 * 
 * Custom exception thrown when the cache is in an illegal state because
 * the user of the class has not checked whether the year is in the cache
 * first.
 * 
 * @author Stephen
 * @version 1.0
 * 
 */
package org.stevie.ddsm.diaries.service.bank;

/**
 * Cache Failure Exception Class
 * 
 * Thrown when the cache is in  an illegal state
 * 
 */
public final class CacheFailureException extends Exception {

	/*
	 * information applicable to this exception 
	 */
	private final int year;
	
	/**
	 * Copy Constructor
	 * 
	 * Constructor must be called with the year of the retrieval that failed.
	 *  
	 * @param year
	 * @since 1.0
	 */
	public CacheFailureException(int year) {
		super("Could not find bank holidays in cache for specified year " + Integer.toString(year));
		this.year = year;
	} 
	
	/**
	 * Year Getter
	 * 
	 * Retrieve year. Not currently in use. 
	 * 
	 * @return year
	 * @since 1.0
	 */
	public int getYear() {
		return this.year;
	}
}
