/**
 * Main Form Controller Class
 * 
 * Controller for main.fxml view. This is the backing controller for the main form. The UI is linked to a JavaFX bean which
 * is bound to the UI. The inputs from this form are passed to the model for diary generation.
 * 
 * @author Stephen
 * @version 1.0
 * 
 */
package org.stevie.ddsm.diaries.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.stevie.ddsm.diaries.domain.DuplicationDiary;
import org.stevie.ddsm.diaries.domain.RecordingDiary;
import org.stevie.ddsm.diaries.ui.FormBean;
import org.stevie.ddsm.diaries.ui.MainFormBackingBean;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.util.StringConverter;

@Component
public class MainFormController  implements Initializable, FormBean {
	
	private static final String DIARY_ERROR_TITLE = "Diary Error";
	private static final String DATE_ERROR_HEADER = "Selected date error";
	private static final String YEAR_ERROR_HEADER = "Year out of bounds";
	private static final String EDITION_ERROR_HEADER = "Edition input error";
	
	/**
	 * Spring bean which is bound to the UI at run-time
	 */
	@Autowired
	private MainFormBackingBean backingBean;
	
	/**
	 * JavaFX controls
	 */
	@FXML
	private ChoiceBox<Integer> diaryYearChoiceBox;
	@FXML
	private TextField magazineEditionTextField;
	@FXML
	private DatePicker januaryEditionDatePicker;
	@FXML
	private TextField decaniCompilerTextField;
	@FXML
	private TextField decaniAssistantCompilerTextField;
	@FXML
	private TextField cantorisCompilerTextField;
	@FXML
	private TextField cantorisAssistantCompilerTextField;
	@FXML
	private Button generateRecordingDiaryButton;
	@FXML
	private Button generateDuplicationDiaryButton;
	@FXML
	private Button bankHolidaysButton;
	@FXML
	private Label internetConnectionLabel;

	/**
	 * Initialise Form
	 * 
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		/*
		 * Allow user to select a year 5 years in the past or 5 years in the future
		 */
		diaryYearChoiceBox.setItems(createYearList());
		diaryYearChoiceBox.getSelectionModel().select(5);
		
		/*
		 * When the user selects a year in the drop down list initialise the date picker 
		 * to the first day of the selected year
		 */
		diaryYearChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
			LocalDate start = LocalDate.of(newValue, 1, 1);
			januaryEditionDatePicker.setValue(start);
		});
		
		/*
		 * Initialise the date picker when the form is first displayed 
		 */
		januaryEditionDatePicker.setValue(LocalDate.of(diaryYearChoiceBox.getValue(), 1, 1));
		
		/*
		 * Set tooltips on the controls
		 */
		diaryYearChoiceBox.setTooltip(new Tooltip("Please select a year from choice box"));
		januaryEditionDatePicker.setTooltip(new Tooltip("Input the date of the first recording carried over from the previous year"));
		magazineEditionTextField.setTooltip(new Tooltip("The edition carried forward from December in the previous year"));

		/*
		 * Bind the UI to the backing bean
		 */
		backingBean.diaryYearPropertty().bind(diaryYearChoiceBox.valueProperty());

		IntegerProperty ip = backingBean.magazineEditionProperty();
		StringProperty sp = magazineEditionTextField.textProperty();
		Bindings.bindBidirectional(sp, ip, new StringConverter<Number>() {

			@Override
			public String toString(Number object) {
				return object.toString();
			}

			@Override
			public Number fromString(String string) {
				return Integer.parseInt(string);
			}
		});
		
		backingBean.januaryEditionProperty().bind(januaryEditionDatePicker.valueProperty());
		backingBean.decaniCompilerProperty().bind(decaniCompilerTextField.textProperty());
		backingBean.decaniAssistantCompilerProperty().bind(decaniAssistantCompilerTextField.textProperty());
		backingBean.cantorisCompilerProperty().bind(cantorisCompilerTextField.textProperty());
		backingBean.cantorisAssistantCompilerProperty().bind(cantorisAssistantCompilerTextField.textProperty());

	}

	/**
	 * Create Year List
	 * Create the options that will appear in the drop down list of the year combo box.
	 * 
	 * @return observable list
	 */
	private ObservableList<Integer> createYearList() {
		ObservableList<Integer> list = FXCollections.observableArrayList();
		int todayYear = LocalDate.now().getYear();
		for (int i = todayYear-5; i<=todayYear+5; i++) {
			list.add(i);
		}
		return list;
	}

	/**
	 * Recording Diary Button Handler
	 * 
	 * On button click validate the form and generate the diary using the domain model. Display
	 * results in dialog box.
	 * 
	 * @param action event
	 * @throws IOException 
	 */
	@FXML
	public void handleGenerateRecordingDiaryButtonAction(ActionEvent event) {
		/*
		 * validate form
		 */
		if (!validateForm()) return;

		/*
		 * create recording diary (builder pattern)
		 */
		var recordingDiary = new RecordingDiary.RecordingDiaryBuilder()
				.diaryYear(backingBean.getDiaryYear())
				.edition(backingBean.getMagazineEdition())
				.januaryEdition(backingBean.getJanuaryEdition())
				.decaniCompiler(backingBean.getDecaniCompiler())
				.decaniAssistant(backingBean.getDecaniAssistantCompiler())
				.cantorisCompiler(backingBean.getCantorisCompiler())
				.cantorisAssistant(backingBean.getCantorisAssistantCompiler())
				.build();
		
		/*
		 * generate diary entries
		 */
		recordingDiary.generateDiary();
		
		/*
		 * display dialog
		 */
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/recording.fxml"));
			DialogPane pane = loader.load();
			RecordingDiaryController controller = loader.getController();
			controller.setDiaryItems(recordingDiary.getEntries());
			controller.setYear(recordingDiary.getDiaryYear());

			pane.getButtonTypes().add(ButtonType.CLOSE);
			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setTitle("Recording Dates");
			dialog.setDialogPane(pane);
			dialog.showAndWait()
				.filter(response -> response == ButtonType.CLOSE)
				.ifPresent(System.out::println);
		} catch (IOException e) {
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle("Fatal Error");
			alert.setHeaderText("Error Loading Form");
			alert.setContentText("An exception occurred during the form loading process. " + e.getMessage());
			alert.showAndWait();
		}
		/*
		 * print to console
		 */
//		recordingDiary.printDiaryToConsole();
	}

	/**
	 * Duplication Diary Button Handler
	 * 
	 * On button click validate the form and generate the diary using the domain model
	 * 
	 * @param action event
	 */
	@FXML
	public void handleGenerateDuplicationDiaryButtonAction(ActionEvent event) {

		/*
		 * validate form
		 */
		if (!validateForm()) return;
		
		/*
		 * create recording diary dependency (builder pattern)
		 */
		var recordingDiary = new RecordingDiary.RecordingDiaryBuilder()
				.diaryYear(backingBean.getDiaryYear())
				.edition(backingBean.getMagazineEdition())
				.januaryEdition(backingBean.getJanuaryEdition())
				.decaniCompiler(backingBean.getDecaniCompiler())
				.decaniAssistant(backingBean.getDecaniAssistantCompiler())
				.cantorisCompiler(backingBean.getCantorisCompiler())
				.cantorisAssistant(backingBean.getCantorisAssistantCompiler())
				.build();
		
		/*
		 * generate diary entries
		 */
		recordingDiary.generateDiary();
		
		/*
		 * generate duplication diary (builder pattern)
		 */
		var duplicationDiary = new DuplicationDiary.DuplicationDiaryBuilder()
				.recordingDiary(recordingDiary)
				.build();
		
		/*
		 * generate diary entries
		 */
		duplicationDiary.generateDiary();
		
		/*
		 * print to console
		 */
		duplicationDiary.printDiaryToConsole();
	}

	@FXML
	public void handleBankHolidaysButtonAction(ActionEvent event) {
	}
	
	/**
	 * Validate Form
	 * 
	 * This method validates user input on the JavaFX form. If a validation step fails the method returns false. If
	 * all the validation steps have passed the method returns true.
	 * 
	 * @return validation status false=failed, true=passed
	 */
	@Override
	public boolean validateForm() {
		
		/*
		 * check year in bounds
		 */
		int year = diaryYearChoiceBox.getValue();
		if (year < 2000 || year > 2500) {
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(DIARY_ERROR_TITLE);
			alert.setHeaderText(YEAR_ERROR_HEADER);
			alert.setContentText("Year should be between 2000 and 2500");
			alert.showAndWait();
			return false;
		}

		/*
		 * check edition numeric and in bounds 
		 */
		try {
			var s = magazineEditionTextField.getText();
			int edition = Integer.parseInt(s);
			if (edition <= 0) {
				var alert = new Alert(AlertType.ERROR);
				alert.setTitle(DIARY_ERROR_TITLE);
				alert.setHeaderText(EDITION_ERROR_HEADER);
				alert.setContentText(String.format("Edition should be greater than 0. You entered %d", edition));
				alert.showAndWait();
				return false;
			}
		} catch (NumberFormatException ex) {
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(DIARY_ERROR_TITLE);
			alert.setHeaderText(EDITION_ERROR_HEADER);
			alert.setContentText("Edition field should be numeric");
			alert.showAndWait();
			return false;
		}
		
		var date = januaryEditionDatePicker.getValue();

		/*
		 * check january edition date has been entered
		 */
		if (date == null) {
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(DIARY_ERROR_TITLE);
			alert.setHeaderText(DATE_ERROR_HEADER);
			alert.setContentText("Please enter a date");
			alert.showAndWait();
			return false;
		}
		
		var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		var formattedDate = date.format(formatter);

		/*
		 * check january year and diary year match
		 */
		if (date.getYear() != year) {
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(DIARY_ERROR_TITLE);
			alert.setHeaderText(DATE_ERROR_HEADER);
			alert.setContentText(String.format("The selected date %s should match selected year %d", formattedDate, year));
			alert.showAndWait();
			return false;
		}

		/*
		 * check selected date is in January
		 */
		if (date.getMonth() != Month.JANUARY) {
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(DIARY_ERROR_TITLE);
			alert.setHeaderText(DATE_ERROR_HEADER);
			alert.setContentText(String.format("The selected date %s should be in January not %s", formattedDate, date.getMonth()));
			alert.showAndWait();
			return false;
		}
		
		/*
		 * make sure january date falls on a Monday or Tuesday
		 */
		if (date.getDayOfWeek() != DayOfWeek.MONDAY && date.getDayOfWeek() != DayOfWeek.TUESDAY) {
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(DIARY_ERROR_TITLE);
			alert.setHeaderText(DATE_ERROR_HEADER);
			alert.setContentText(String.format("The selected date %s should fall on a Monday or Tuesday. You entered a date that falls on a %s", formattedDate, date.getDayOfWeek()));
			alert.showAndWait();
			return false;
		}
		
		return true;
	}
}
