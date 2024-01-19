/**
 * JavaFX Application Class
 * 
 * The fundamental part of this application is integrating JavaFX and Spring which is used for 
 * dependency injection. It creates a spring application context and publishes an event 
 * when the JavaFX runtime is ready.
 * 
 * @author Stephen
 * @version 1.0
 * 
 */
package org.stevie.ddsm.diaries;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;


/**
 * Application Subclass
 * 
 * JavaFX entry point
 * 
 */
public class DiaryApplicationFX extends Application {

	/**
	 * spring application context
	 */
	private ConfigurableApplicationContext applicationContext;

	/**
	 * logging
	 */
	private static Logger logger = LoggerFactory.getLogger(DiaryApplicationFX.class);

	/**
	 * Application Start Method
	 * 
	 * This method is called after the init method during startup. It publishes an event which is
	 * captured by the {@link org.stevie.dsm.diaries.StageInitialiser} class which displays the
	 * main stage.  
	 * 
	 * @param primary stage
	 * @since 1.0
	 * 
	 */
	@Override
	public void start(Stage stage) {
		logger.info("Diary application started");
		applicationContext.publishEvent(new StageReadyEvent(stage));
	}
	
	/**
	 * Application Init Method
	 * 
	 * This method is invoked after the JavaFX runtime has been started and an instance of the
	 * Application class has been created. It creates the application context.
	 * 
	 * @since 1.0
	 * 
	 */
	@Override
	public void init() {
		logger.info("Initialise application context");
		applicationContext = new SpringApplicationBuilder(DdsmDiariesApplication.class).run();
	}
	
	/**
	 * Application Stop Method
	 * 
	 * This method is invoked when the Application is being shut down. This is when the
	 * last window has been closed. It shuts down the application context in an orderly way making sure
	 * all spring beans are deleted.
	 * 
	 * @since 1.0
	 */
	@Override
	public void stop() {
		logger.info("Diary application stopped");
		applicationContext.close();
		Platform.exit();
	}
	
	/**
	 * Stage Ready Event Class
	 * 
	 * This class is an inner class derived from ApplicationEvent. It is fired during program startup.
	 * It carries a reference to the primary stage of the application.
	 * 
	 */
	static class StageReadyEvent extends ApplicationEvent {
		
		private static final long serialVersionUID = 1L;

		/**
		 * Copy Constructor
		 *  
		 * @param primary stag
		 * @since 1.0
		 * 
		 */
		public StageReadyEvent(Stage stage) {
			super(stage);
		}

		/**
		 * Stage Getter Method
		 * 
		 * Return the primary stage of the application in the event pay-load
		 * 
		 * @return primary stage
		 */
		public Stage getStage() {
			return (Stage)getSource();
		}
		
	}

}
