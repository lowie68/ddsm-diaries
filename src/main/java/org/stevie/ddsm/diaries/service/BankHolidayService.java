/**
 * Bank Holiday Service Class
 * 
 * This class provides bank holiday support to the application. It uses the nager holiday REST API which 
 * fetches the bank holidays in a given year and country. Bank holidays are loaded into a cache on first use.
 * The main purpose of this class is to check whether a given date is a bank holiday. This allows the main
 * application to reschedule any diary dates to the following week usually.
 * 
 * @author Stephen
 * @version 1.0
 * 
 */
package org.stevie.ddsm.diaries.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.stevie.ddsm.diaries.domain.BankHoliday;

import reactor.netty.http.client.HttpClient;

@Service
public final class BankHolidayService {

	private BankHolidayService() {
		
	}
	
	/*
	 * bank holiday map cache
	 */
	private static Map<Integer, List<BankHoliday>> bankHolidayCache = new HashMap<>();

	/*
	 * Spring webflux web client
	 */
	private static WebClient webClient;

	static {
		
		/*
		 * inform user an internet connection has been successful
		 */
		HttpClient httpClient = HttpClient.create().doOnConnected(conn -> System.out.println("connected!"));

		/*
		 * create Spring WebFlux client
		 */
		webClient = WebClient.builder()
				.baseUrl("https://date.nager.at/api/v2")
		        .clientConnector(new ReactorClientHttpConnector(httpClient))
		        .build();	
	}

	/**
	 * Check if date is a bank holiday
	 * 
	 * Return true if given date is a bank holiday in England. 
	 * 
	 * @param date ti check
	 * @return true if date is a bank holiday
	 */
	public static boolean isBankHoliday(LocalDate date) {
		int year = date.getYear();
		
		/*
		 * if there are no dates for given year in cache load them from the REST API
		 */
		if (!bankHolidayCache.containsKey(year)) {
			List<BankHoliday> list = fetchBankHolidaysFromInternet(year);
			bankHolidayCache.put(year, list);
		}
		
		/*
		 * Retrieve bank holidays for given year from the cache
		 */
		List<BankHoliday> bankHolidays = bankHolidayCache.get(year);
		
		/*
		 * return true if there is a match
		 */	
		return bankHolidays.stream().anyMatch(bh -> bh.getDate().isEqual(date));
	}

	/**
	 * Fetch Bank Holidays 
	 * 
	 * Issue GET request to REST API. Filter out holidays for Scotland, Northern Ireland and Wales.
	 * Sort into ascending date order. Collect into a List.
	 * 
	 * @param year
	 * @return List of bank holidays for given year
	 */
	private static List<BankHoliday> fetchBankHolidaysFromInternet(int year) {
		return webClient.get()
				.uri("/PublicHolidays/{year}/GB", year).accept(MediaType.APPLICATION_JSON)
				.retrieve()
			    .bodyToFlux(BankHoliday.class)
			    .filter(bh -> {
			        	if (bh.getCounties() == null) return true;
			        	for (var county : bh.getCounties()) {
			        		if (county.contains("GB-ENG")) 
			        			return true;
			        	}
			        	return false;
			        })
			    .collectSortedList(Comparator.comparing(BankHoliday::getDate))
			    .doOnError(ex -> System.out.println(ex))
			    .block();
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
