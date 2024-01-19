/**
 * <h3>Fetch Bank Holiday Task</h3>
 * 
 * <p>This class extends the JavaFX Task class which is a fully observable implementation 
 * of a {@link FutureTask} with additional state and observable properties useful for programming 
 * asynchronous tasks in JavaFX, as defined in the {@link Worker} interface. It overrides
 * the {@link javafx.concurrent.Task#call()} method which is invoked on the background thread.</p>
 * 
 * @author Stephen
 * @version 1.0
*/
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

/**
 * Fetch Bank Holiday Class
 * 
 * Subclass of {@link javafx.concurrent.Task}. This task is executed on a background thread
 * so as not to tie up the JavaFX Application Thread.
 *  
 */
public final class FetchBankHolidaysTask extends Task<List<BankHoliday>> {

	/*
	 * logger
	 */
	private static Logger logger = LoggerFactory.getLogger(FetchBankHolidaysTask.class);

	/*
	 * reactive version of RestTemplate which is no longer supported. It is used to make 
	 * network calls to nager date api
	 */
	private static WebClient webClient;
	
	/*
	 * the year to fetch holidays for
	 */
	private Integer year;

	/*
	 * Static Constructor
	 * 
	 * Ensure the web client is only created once when the class is first used.
	 */
	static {
		
		/*
		 * create the http client
		 */
		HttpClient httpClient = HttpClient.create()
				.doOnConnected(conn -> logger.info("Connected to internet!"));

		/*
		 * create the web client
		 */
		webClient = WebClient.builder()
				.baseUrl("https://date.nager.at/api/v3")
		        .clientConnector(new ReactorClientHttpConnector(httpClient))
		        .build();	
	}

	/**
	 * Copy Constructor
	 * 
	 * Initialise year member to be used during network call
	 * 
	 * @param the year to fetch the bank holidays for
	 * @since 1.0
	 * 
	 */
	public FetchBankHolidaysTask(int year) {
		this.year = year;
	}
	
	/**
	 * Call Method
	 * 
	 * Overrides the {@link javafx.concurrent.Task#call()} method. It makes an http request to
	 * the nager date rest api. It waits for the json response which is converted into a 
	 * reactor flux. The flux is filtered to include only bank holidays in England. These
	 * are then sorted into ascending date order. If this is successful the bank holidays
	 * are put in memory for subsequent access. If there is a fault an error is written to the log file.
	 * 
	 * @return a list of bank holidays for specified year 
	 * 
	 */
	@Override
	protected List<BankHoliday> call() throws Exception {
		
		/*
		 * log message
		 */
		logger.info("Retrieving bank holidays for year {}", this.year);
		
		/*
		 * rest api call
		 */
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
