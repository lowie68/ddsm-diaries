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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import static org.stevie.ddsm.diaries.DiaryApplicationFX.StageReadyEvent;

import java.io.IOException;
@Component
public class StageInitialiser implements ApplicationListener<StageReadyEvent> {
	
	@Value("classpath:/fxml/main.fxml")
	private Resource mainResource;
	@Value("classpath:/css/styles.css")
	private Resource cssResource;
	
	private String applicationTitle;
	private ApplicationContext applicationContext;
	
	private StageInitialiser(@Value("${spring.application.ui.title}") String applicationTitle, 
									ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		this.applicationTitle = applicationTitle;
	}

	@Override
	public void onApplicationEvent(StageReadyEvent event) {
		
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(mainResource.getURL());
			fxmlLoader.setControllerFactory(aClass -> applicationContext.getBean(aClass));
			Parent parent = fxmlLoader.load();
			Scene scene = new Scene(parent, 800, 600);
		    Stage stage = event.getStage();
			stage.setTitle(applicationTitle);
			stage.setScene(scene);
			stage.show();
		} catch (IOException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Fatal Error");
			alert.setHeaderText("I/O Exception occured when loading main.fxml");
			alert.setContentText("An exception (" + e.getMessage() + ") occured when loading the main form. The program will close. Please inform software engineer.");
			alert.showAndWait();
		}
		
	}

}
