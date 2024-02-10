/**
 * Stage Initialiser Class
 * 
 * This class listens for a {@link org.stevie.ddsm.diaries.DiaryApplicatioFX.StageReadyEvent}.
 * When the event is fired this class will load the main window of the application.
 * 
 * @author Stephen
 * @version 1.0 
 * 
 */
package org.stevie.ddsm.diaries;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.stevie.ddsm.diaries.DiaryApplicationFX.StageReadyEvent;
import org.stevie.ddsm.diaries.messages.ErrorMessages;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

@Component
public class StageInitialiser implements ApplicationListener<StageReadyEvent> {

	private static Logger logger = LoggerFactory.getLogger(StageInitialiser.class);

	/**
	 * application context
	 */
	private ApplicationContext applicationContext;
	
	/**
	 * Copy Constructor
	 * 
	 * Stores the application context reference
	 * 
	 * @param application context reference
	 * @since 1.0
	 */
	public StageInitialiser(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * Event Handler
	 * 
	 * This method is invoked when the {@link org.stevie.ddsm.diaries.DiaryApplication.StageReadyEvent}
	 * is fired during application start up. It parses the fxml file and displays the main stage.
	 * 
	 * @param stage ready event
	 * @since 1.0
	 */
	@Override
	public void onApplicationEvent(StageReadyEvent event) {
		
		try {
			logger.info("Loading main stage from fxml");
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
			fxmlLoader.setControllerFactory(aClass -> applicationContext.getBean(aClass));
			Parent parent = fxmlLoader.load();
			Scene scene = new Scene(parent, 800, 600);
		    Stage stage = event.getStage();
			stage.setTitle("DDSM Rota Creator");
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			logger.error("I/O exception occurred while loading main stage - {}", e);
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(ErrorMessages.FATAL_ERROR);
			alert.setHeaderText(ErrorMessages.FORM_LOAD_ERROR);
			alert.setContentText("An exception occurred during the main stage loading process. See log file for details!");
			alert.showAndWait();
			Platform.exit();
		}
		
	}

}
