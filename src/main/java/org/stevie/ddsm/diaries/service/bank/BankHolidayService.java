/**
 * <h3>Bank Holiday Service Class<h3>
 * 
 * <p>This class provides bank holiday support to the application. It uses the nager holiday 
 * REST API which fetches the bank holidays in a given year and country. Bank holidays are 
 * then loaded into a cache on first use. The main purpose of this class is to check whether a 
 * given date is a bank holiday. This allows the main application to reschedule any diary dates 
 * to the following week usually.</p>
 * 
 * @author Stephen
 * @version 1.0
 * 
 */
package org.stevie.ddsm.diaries.service.bank;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bank Holiday Service Class
 * 
 * Provide a service to the main application for checking whether a date falls on a bank holiday.
 * 
 */
public final class BankHolidayService {

	/*
	 * logging
	 */
	private static Logger logger = LoggerFactory.getLogger(BankHolidayService.class);

	/**
	 * Default Constructor
	 * 
	 * Prevents any one from creating instances of this class.
	 * 
	 * @since 1.0
	 */
	private BankHolidayService() {
		
	}
	

	/**
	 * Is A Bank Holiday Method
	 * 
	 * Return true if given date is a bank holiday in England.
	 * 
	 * @param date to check
	 * @return true if date is on a bank holiday
	 * @since 1.0
	 */
	public static boolean isBankHoliday(LocalDate date) {
		
		/*
		 * store year in local variable
		 */
		int year = date.getYear();

		/*
		 * Create empty list
		 */
		List<BankHoliday> bankHolidays = Collections.emptyList();
		
		/*
		 * Check if the bank holidays for this year are in the cache
		 */
		if (BankHolidayCache.isInCache(year)) { //year is in the cache
			try {
				/*
				 * retrieve the bank holidays from memory
				 */
				bankHolidays = BankHolidayCache.getBankHolidaysFromCache(year);
			} catch (CacheFailureException e) {
				/*
				 * This should never happen. Check whether a year is in the cache
				 * first before attempting to retrieve it. Log the error. 
				 */
				logger.error("Cache retrieval failed for year {}", year);
			}
		}
		else { //year is not in the cache
			/*
			 * create the background task to fetch thye bank holidays from the REST
			 * API
			 */
			FetchBankHolidaysTask task = new FetchBankHolidaysTask(year);
			
			/*
			 * if the background task fails log an error
			 */
			task.setOnFailed(ev -> {
				logger.error("Bank holiday task failed while trying to retrieve bank holidays for year {} ", year);
			});
			
			try (ExecutorService executor = Executors.newCachedThreadPool()) {
				/*
				 * submit the task
				 */
				executor.submit(task);
				/*
				 * retrieve the result. If it takes longer than ten seconds throw 
				 * a {@link TimeoutException}
				 */
				bankHolidays = task.get(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				/*
				 * Thrown when a thread is waiting, sleeping, or otherwise occupied
			 	 * and the thread is interrupted.
				 */
				logger.error("Bank holiday task cancelled while trying to retrieve bank holidays for year {}", year);
				Thread.currentThread().interrupt();
			} catch (ExecutionException e) {
				/*
				 * Exception thrown when attempting to retrieve the result of a task 
				 * that aborted by throwing an exception. This exception can be inspected using 
				 * the {@link #getCause()} method.
				 */
				logger.error("Bank holiday task failed while trying to retrieve bank holidays for year {}", year);
			} catch (TimeoutException e) {
				/*
				 * Exception thrown when a blocking operation times out.  Blocking
				 * operations for which a timeout is specified need a means to
				 * indicate that the timeout has occurred. 
				 */
				logger.error("Bank holiday task timed out while trying to retrieve bank holidays for year {}", year);
			}
			
		}
		/*
		 * check if the date is in the list of bank holiday using JDK 8 lambda expression
		 */
		return bankHolidays.stream().anyMatch(bh -> bh.getDate().isEqual(date));

	}

	/**
	 * Get Next Non Bank Holiday Method
	 * 
	 * Loop through dates by week until we find a date which is not a bank holiday.
	 * Uses imperative style.
	 * 
	 * @param bank holiday date
	 * @return next non bank holiday
	 */
	public static LocalDate getNextNonBankHoliday(LocalDate bankHoliday) {
		int storeMonth = bankHoliday.getMonthValue();
		var currentDate = bankHoliday;
		/*
		 * loop through until the next non bank holiday
		 * is found
		 */
		while (isBankHoliday(currentDate) && currentDate.getMonthValue() == storeMonth) {
			currentDate = currentDate.plusWeeks(1L);
		}
		return currentDate;
	}
	
}
