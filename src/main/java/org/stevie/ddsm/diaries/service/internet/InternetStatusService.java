/**
 * <h3>Internet Status Service Class<h3>
 * 
 * <p>This class executes in the background every 30 seconds. It checks whether an Internet connection
 * is available. If there is a connection available it updates the UI which relays
 * to the end user whether the Internet is up or down.</p>
 * 
 * @author Stephen
 * @version 1.0
 */
package org.stevie.ddsm.diaries.service.internet;

import java.io.IOException;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;

/**
 * Internet Status Service Class
 */
public final class InternetStatusService extends ScheduledService<InternetStatus> {

	public static final String INTERNET_AVAILABLE = "Internet connection available";
	public static final String INTERNET_NOT_AVAILABLE = "No internet connection available";
	public static final String INTERNET_STATUS_CHECK_FAILED = "Internet status check failed";
	public static final String IN_PROGRESS = "Checking For Internet...";
	
    @Override
    protected Task<InternetStatus> createTask() {
      return new Task<InternetStatus>() {

        @Override
        protected InternetStatus call() throws IOException {
        	
        	updateMessage(IN_PROGRESS);
  			if (InternetService.isInternetAvailable()) {
  				updateMessage(INTERNET_AVAILABLE);
  				return InternetStatus.INTERNET_UP;
  			} else {
  				updateMessage(INTERNET_NOT_AVAILABLE);
  				return InternetStatus.INTERNET_DOWN;
  			}
        }
      };
    }  
}
