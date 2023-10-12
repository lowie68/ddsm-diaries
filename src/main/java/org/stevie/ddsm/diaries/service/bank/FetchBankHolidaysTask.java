package org.stevie.ddsm.diaries.service.bank;

import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import javafx.concurrent.Task;
import reactor.netty.http.client.HttpClient;

public final class FetchBankHolidaysTask extends Task<List<BankHoliday>> {

	private static Logger logger = LoggerFactory.getLogger(FetchBankHolidaysTask.class);

	/*
	 * Spring webflux web client
	 */
	private static WebClient webClient;
	
	private Integer year;

	static {
		
		/*
		 * inform user an internet connection has been successful
		 */
		HttpClient httpClient = HttpClient.create()
				.doOnConnected(conn -> logger.info("Connected to internet!"));

		/*
		 * create Spring WebFlux client
		 */
		webClient = WebClient.builder()
				.baseUrl("https://date.nager.at/api/v3")
		        .clientConnector(new ReactorClientHttpConnector(httpClient))
		        .build();	
	}

	public FetchBankHolidaysTask(int year) {
		this.year = year;
	}
	
	@Override
	protected List<BankHoliday> call() throws Exception {
		
		if (BankHolidayCache.isInCache(this.year)) {
			try {
				return BankHolidayCache.getBankHolidaysFromCache(this.year);
			} catch (CacheFailureException e) {
				logger.error("Cache retrieval failed for year {}", this.year);
			}
		}
		
		logger.info("Retrieving bank holidays for year {}", this.year);
		
		return webClient.get()
				.uri("/PublicHolidays/{year}/GB", this.year).accept(MediaType.APPLICATION_JSON)
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
			    .doOnSuccess(list -> BankHolidayCache.putBankHolidaysInCache(list, this.year))
			    .doOnError(ex -> logger.error("Error retrieving bank holidays from REST API exception was {}", ex.getMessage()))
			    .block();
	}

}
