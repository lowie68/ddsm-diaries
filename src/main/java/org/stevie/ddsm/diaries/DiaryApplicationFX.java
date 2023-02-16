/**
 * JavaFX entry point
 * 
 * The fundamental part of this application is integrating JavaFX and Spring which is used for dependency injection. It creates a 
 * spring application context and publishes an event when JavaFX is ready.
 * 
 * @author Stephen
 * @version 1.0
 * 
 */
package org.stevie.ddsm.diaries;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class DiaryApplicationFX extends Application {

	private ConfigurableApplicationContext applicationContext;

	/**
	 * JavaFX start method
	 * 
	 * Publish event when JavaFX is ready. The stage initialiser is listening for the event and will load
	 * the main form when the event is received.
	 * 
	 * @param primary stage
	 */
	@Override
	public void start(Stage stage) {
		applicationContext.publishEvent(new StageReadyEvent(stage));
	}
	
	/**
	 * JavaFX init method
	 * 
	 * Creates the spring application context.
	 * 
	 */
	@Override
	public void init() {
		applicationContext = new SpringApplicationBuilder(DdsmDiariesApplication.class).run();
	}
	
	/**
	 * JavaFX stop method
	 * 
	 * Ensure the application context is closed and spring beans have been deleted on shut down of the 
	 * application.
	 * 
	 */
	@Override
	public void stop() {
		applicationContext.close();
		Platform.exit();
	}
	
	/**
	 * Stage Ready Event Inner class
	 * 
	 * Custom event which is published when the stage is ready
	 * 
	 */
	static class StageReadyEvent extends ApplicationEvent {
		
		private static final long serialVersionUID = 1L;

		public StageReadyEvent(Stage stage) {
			super(stage);
		}

		/**
		 * Retrieve the stage the event is referring to
		 * 
		 * @return primary stage
		 */
		public Stage getStage() {
			return (Stage)getSource();
		}
		
	}

}
