/**
 * <h3>Recording Rota Dialog</h3>
 * 
 * <p>This class is the MVC controller for the preparation and duplication dialog.
 * The dialog presents the recording rota to the end user</p>
 * 
 * @author Stephen
 * @version 1.0
 * 
 */
package org.stevie.ddsm.diaries.controllers;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;
import org.stevie.ddsm.diaries.domain.RecordingDiaryEntry;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Recording Rota Dialog Class
 * 
 * JavaFX controller backing the view in recording.fxml.
 * 
 */
@Component
public final class RecordingDiaryController implements Initializable {
	/*
	 * JavaFX Controls
	 */
	@FXML
	private Label dialogHeaderLabel;
	@FXML
	private TableView<RecordingDiaryEntry> diaryTableView;
    @FXML
    private TableColumn<RecordingDiaryEntry, String> dateColumn;
    @FXML
    private TableColumn<RecordingDiaryEntry, Integer> editionColumn;
    @FXML
    private TableColumn<RecordingDiaryEntry, String> compilerColumn;
    @FXML
    private TableColumn<RecordingDiaryEntry, String> monthColumn;
    
    /**
 	 * Initialise Controller
 	 * 
 	 * Called to initialise a controller after its root element has been completely processed.
 	 * 
 	 * @param url
 	 * @param resources
 	 * @since 1.0
 	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {

		/*
		 * call private method
		 */
		configureTableView();
	}

	/**
	 * Configure Table View Method
	 * 
	 * This method configures the JavaFX {@code TableView}. The {@code TableView} has four columns.
	 * The first column displays the month. The second column is the date of recording which
	 * is always a Monday except bank holidays. The third column is the magazine edition. The
	 * fourth column displays the compiler which alternates between compiler 1 and compiler 2.
	 * 
	 * @since 1.0
	 */
	private void configureTableView() {

		/*
		 * month column Jan-Dec
		 */
		monthColumn.setCellValueFactory(rde -> new SimpleStringProperty(rde.getValue().month().toString()));
	
		/*
		 * date column (date + day of week)
		 */
		dateColumn.setCellValueFactory(rde -> {
			var date = rde.getValue().recordingDate();
			var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			var dateString = formatter.format(date);
			var resultString = "(" + date.getDayOfWeek().toString() + ") " + dateString;
			return new SimpleStringProperty(resultString);
		});	
		
		/*
		 * edition column
		 */
		editionColumn.setCellValueFactory(rde -> new SimpleIntegerProperty(rde.getValue().edition()).asObject());

		/*
		 * compiler column
		 */
		compilerColumn.setCellValueFactory(rde -> new SimpleStringProperty(rde.getValue().compiler()));

	}

	/**
	 * Set Diary Items Method
	 * 
	 * This method sets the {@link TableView} model. It is called before the stage is
	 * displayed from the {@link MainFormController}.

	 * @param collection of diary items
	 * @since 1.0
	 */
	public void setDiaryItems(List<RecordingDiaryEntry> items) {
		Objects.requireNonNull(items);
		var diaryItems = FXCollections.observableArrayList(items);
		diaryTableView.setItems(diaryItems);
	}
	
	/**
	 * Set Diary Year Method
	 * 
	 * This method sets the active year in the dialog header. It is called before the stage
	 * is displayed by the {@link MainFormController}.
	 * 
	 * @param active year
	 * @since 1.0
	 */
	public void setYear(int year) {
		/*
		 * set the Label text property
		 */
		dialogHeaderLabel.setText(dialogHeaderLabel.getText()+ " " + Integer.toString(year));
	}

}
