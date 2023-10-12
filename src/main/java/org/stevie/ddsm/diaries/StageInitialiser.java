/**
 * Stage Initialiser Class
 * 
 * When a stage ready event is fired during application startup, this class will load the main form for the application.
 * The view is separated from the business logic using JavaFX fxml language. This class is a spring bean which is
 * created when the application context is initialised.
 * 
 * @author Stephen
 * @version 1.0 
 * 
 */
package org.stevie.ddsm.diaries;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.stevie.ddsm.diaries.DiaryApplicationFX.StageReadyEvent;

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

	@Value("classpath:/fxml/main.fxml")
	private Resource mainResource;
	
	private String applicationTitle;
	
	private ApplicationContext applicationContext;
	
	public StageInitialiser(@Value("${spring.application.ui.title}") String applicationTitle, 
									ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		this.applicationTitle = applicationTitle;
	}

	@Override
	public void onApplicationEvent(StageReadyEvent event) {
		
		try {
			logger.info("Loading main form from fxml");
			FXMLLoader fxmlLoader = new FXMLLoader(mainResource.getURL());
			fxmlLoader.setControllerFactory(aClass -> applicationContext.getBean(aClass));
			Parent parent = fxmlLoader.load();
			Scene scene = new Scene(parent, 800, 600);
		    Stage stage = event.getStage();
			stage.setTitle(applicationTitle);
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			logger.error("I/O exception occurred while loading main form - {}", e.getMessage());
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle("Fatal Error");
			alert.setHeaderText("Error Loading Form");
			alert.setContentText("An exception occurred during the main form loading process. See log file for details!");
			alert.showAndWait();
			Platform.exit();
		}
		
	}

}
