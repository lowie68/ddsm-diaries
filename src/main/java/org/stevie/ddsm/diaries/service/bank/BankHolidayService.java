/**
 * <h3>Bank Holiday Service Class<h3>
 * 
 * <p>This class provides bank holiday support to the application. It uses the nager holiday REST API which 
 * fetches the bank holidays in a given year and country. Bank holidays are loaded into a cache on first use.
 * The main purpose of this class is to check whether a given date is a bank holiday. This allows the main
 * application to reschedule any diary dates to the following week usually.</p>
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
import org.springframework.stereotype.Service;

@Service
public final class BankHolidayService {

	private static Logger logger = LoggerFactory.getLogger(BankHolidayService.class);

	private BankHolidayService() {
		
	}
	

	/**
	 * Check if date is a bank holiday
	 * 
	 * Return true if given date is a bank holiday in England. 
	 * 
	 * @param date to check
	 * @return true if date is on a bank holiday
	 */
	public static boolean isBankHoliday(LocalDate date) {
		
		int year = date.getYear();

		List<BankHoliday> bankHolidays = Collections.emptyList();
		
		if (BankHolidayCache.isInCache(year)) {
			try {
				bankHolidays = BankHolidayCache.getBankHolidaysFromCache(year);
			} catch (CacheFailureException e) {
				logger.error("Cache retrieval failed for year {}", year);
			}
		}
		else {
			FetchBankHolidaysTask task = new FetchBankHolidaysTask(year);
			task.setOnFailed(ev -> {
				logger.error("Bank holiday task failed while trying to retrieve bank holidays for year {} ", year);
			});
			try (ExecutorService executor = Executors.newCachedThreadPool()) {
				executor.submit(task);
				bankHolidays = task.get(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.error("Bank holiday task cancelled while trying to retrieve bank holidays for year {}", year);
				Thread.currentThread().interrupt();
			} catch (ExecutionException e) {
				logger.error("Bank holiday task failed while trying to retrieve bank holidays for year {}", year);
			} catch (TimeoutException e) {
				logger.error("Bank holiday task timed out");
			}
			
		}
		
		return bankHolidays.stream().anyMatch(bh -> bh.getDate().isEqual(date));

	}

	/**
	 * Get Next Non Bank Holiday
	 * 
	 * Loop through dates by week until we find a date which is not a bank holiday
	 * 
	 * @param bank holiday
	 * @return next non bank holiday
	 */
	public static LocalDate getNextNonBankHoliday(LocalDate bankHoliday) {
		int storeMonth = bankHoliday.getMonthValue();
		var currentDate = bankHoliday;
		while (isBankHoliday(currentDate) && currentDate.getMonthValue() == storeMonth) {
			currentDate = currentDate.plusWeeks(1L);
		}
		return currentDate;
	}
	
}
