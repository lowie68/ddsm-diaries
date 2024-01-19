/**
 * Main Window Controller Class
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.stevie.ddsm.diaries.domain.DuplicationDiary;
import org.stevie.ddsm.diaries.domain.RecordingDiary;
import org.stevie.ddsm.diaries.messages.ErrorMessages;
import org.stevie.ddsm.diaries.service.bank.BankHolidayService;
import org.stevie.ddsm.diaries.service.internet.InternetStatusService;

import javafx.application.Platform;
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
import javafx.util.Duration;

/**
 * Main Window Controller Class
 * 
 * JavaFX controller backing the view in main.fxml.
 * 
 */
@Component
public class MainFormController  implements Initializable {
	
	/*
	 * logging
	 */
	private static Logger logger = LoggerFactory.getLogger(MainFormController.class);

	/*
	 * JavaFX controls
	 */
	@FXML
	private ChoiceBox<Integer> diaryYearChoiceBox;
	@FXML
	private TextField magazineEditionTextField;
	@FXML
	private DatePicker januaryEditionDatePicker;
	@FXML
	private TextField compiler_1TextField;
	@FXML
	private TextField compiler_2TextField;
	@FXML
	private Button generateRecordingDiaryButton;
	@FXML
	private Button generateDuplicationDiaryButton;
	@FXML
	private Button bankHolidaysButton;
	@FXML
	private Label internetConnectionLabel;

	/*
	 * active year
	 */
	private int currentYear;

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
		configureYearChoiceBox();
		configureJanuaryDatePicker();
		configureMagazineEditionTextField();
		configureInternetStatusChecker();
	}

	/**
	 * Configure Magazine Edition Method
	 * 
	 * The magazine edition is incremented for each month the magazine is published. It rolls
	 * over from the previous year. This method defaults the number presented to the user to 400.
	 * 
	 * @since 1.0
	 * 
	 */
	private void configureMagazineEditionTextField() {
		magazineEditionTextField.setText("400");
		magazineEditionTextField.setTooltip(new Tooltip("The edition carried forward from December in the previous year"));
	}

	/**
	 * Configure January Date Picker Method
	 * 
	 * The date of the first edition of the magazine in a particular year can be specified by the user.
	 * This is known as the january edition. It is initialised to the first day of the first month
	 * of the active year.
	 * 
	 * @since 1.0
	 * 
	 */
	private void configureJanuaryDatePicker() {

		/*
		 * Initialise the date picker to first day of first month of active year
		 */
		januaryEditionDatePicker.setValue(LocalDate.of(diaryYearChoiceBox.getValue(), 1, 1));
		januaryEditionDatePicker.setTooltip(new Tooltip("Input the date of the first recording carried over from the previous year"));
		
	}

	/**
	 * Configure Year Choice Box Method
	 * 
	 * The year choice box allows the end user to specify the year for which the rotas
	 * apply to. This is known as the active year. The user can select either 5 years previous
	 * to the current date or 5 years in advance of the current date.
	 * 
	 * @since 1.0
	 */
	private void configureYearChoiceBox() {

		/*
		 * Allow user to select a year 5 years in the past or 5 years in the future
		 */
		diaryYearChoiceBox.setItems(createYearList());
		
		/*
		 * auto select current year
		 */
		diaryYearChoiceBox.getSelectionModel().select(5);
		
		/*
		 * add a change listener to set active year every time the user selects an item in the drop
		 * down list
		 */
		diaryYearChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> this.currentYear = newValue);

		/*
		 * initialise active year
		 */
		this.currentYear = diaryYearChoiceBox.getValue();
		
		/*
		 * when the user selects a year in the drop down list initialise the date picker 
		 * to the first day of the selected year
		 */
		diaryYearChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
			LocalDate start = LocalDate.of(newValue, 1, 1);
			januaryEditionDatePicker.setValue(start);
		});

		diaryYearChoiceBox.setTooltip(new Tooltip("Please select a year from choice box"));
	}

	/**
	 * Configure Internet Status Checker
	 * 
	 * The main window contains a label that shows whether an Internet connection is available. If not,
	 * the user has to input bank holidays manually. A background task executes every 30 seconds which
	 * checks the Internet Connection.
	 * 
	 * @since 1.0
	 */
	private void configureInternetStatusChecker() {
		
		/*
		 * check Internet status background task {@link InternetStatusService}
		 */
		var service = new InternetStatusService();

		/*
		 * JavaFx scheduled service set on 30s intervals 
		 */
		service.setPeriod(Duration.seconds(30));

		/*
		 * task in progress
		 */
		service.setOnRunning(
			e -> {
				internetConnectionLabel.setText(InternetStatusService.IN_PROGRESS);
				internetConnectionLabel.setStyle("-fx-text-fill: orange;");
			});

		/*
		 * task completed successfully. Update the UI.
		 */
		service.setOnSucceeded(
			e -> {
				switch (service.getValue()) {
					case INTERNET_DOWN -> {
						internetConnectionLabel.setText(InternetStatusService.INTERNET_NOT_AVAILABLE);
						internetConnectionLabel.setStyle("-fx-text-fill: red;");
					}
					case INTERNET_UP -> {
						internetConnectionLabel.setText(InternetStatusService.INTERNET_AVAILABLE);
						internetConnectionLabel.setStyle("-fx-text-fill: green;");
					}
					default -> {
						logger.error("Unrecognised enum {}", service.getValue());
						var alert = new Alert(AlertType.ERROR);
						alert.setTitle(ErrorMessages.FATAL_ERROR);
						alert.setHeaderText(ErrorMessages.ILLEGAL_PROGRAM_STATE);
						alert.setContentText("An unrecognised enum was passed to switch statement. See log file for details.");
						alert.showAndWait();
						Platform.exit();
					}
				}
			});

		/*
		 * task failed display in red 
		 */
		service.setOnFailed(
			e -> {
				internetConnectionLabel.setText(InternetStatusService.INTERNET_STATUS_CHECK_FAILED);
				internetConnectionLabel.setStyle("-fx-text-fill: red;");
				logger.error("Internet status background thread failed with exception {}", service.getException().getMessage());
				var alert = new Alert(AlertType.ERROR);
				alert.setTitle(ErrorMessages.FATAL_ERROR);
				alert.setHeaderText(ErrorMessages.ILLEGAL_PROGRAM_STATE);
				alert.setContentText("The internet status check background thread failed. See log file for details.");
				alert.showAndWait();
			});

		/*
		 * start the task
		 */
		service.start();
	}

	/**
	 * Create Year List Method
	 * 
	 * Create the options that will appear in the drop down list of the year combo box.
	 * 
	 * @return observable list of years
	 * 
	 * @since 1.0
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
	 * Recording Rota Button
	 * Event Handler
	 * 
	 * This method is called when the end user clicks the recording rota button. The
	 * method validates the form and generates the diary using the domain model. The
	 * results are displayed in a dialog box {@link RecordingDiaryController}.
	 * 
	 * @param button click event
	 * 
	 * @since 1.0
	 */
	@FXML
	public void handleGenerateRecordingDiaryButtonAction(ActionEvent event) {
		
		/*
		 * validate form
		 */
		if (!validateForm()) return;

		/*
		 * create recording rota model using builder pattern
		 */
		var recordingDiary = new RecordingDiary.RecordingDiaryBuilder()
				.edition(Integer.valueOf(magazineEditionTextField.getText()))
				.januaryEdition(januaryEditionDatePicker.getValue())
				.compiler_1(compiler_1TextField.getText())
				.compiler_2(compiler_2TextField.getText())
				.build();

		/*
		 * must call this before generating the diary
		 */
		recordingDiary.setYear(this.currentYear);

		/*
		 * generate recording diary entries
		 */
		recordingDiary.generateDiary();
		
		/*
		 * display dialog
		 */
		displayRecordingDiaryDialog(recordingDiary);
		
	}

	/**
	 * 
	 * Display Recording Rota Dialog Method
	 * 
	 * This method displays the results of the rota generator which has created
	 * the recording rota based on user inputs.
	 *  
	 * @param recording rota model
	 * @since 1.0
	 */
	private void displayRecordingDiaryDialog(RecordingDiary recordingDiary) {
		try {
			/*
			 * create the loader & parse fxml file
			 */
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/recording.fxml"));
			DialogPane pane = loader.load();
			/*
			 * add dialog buttons
			 */
			pane.getButtonTypes().add(ButtonType.CLOSE);

			/*
			 * get controller reference and set properties
			 */
			RecordingDiaryController controller = loader.getController();
			controller.setDiaryItems(recordingDiary.getEntries());
			controller.setYear(recordingDiary.getYear());
			/*
			 * create dialog wrapper and set title and pane root
			 */
			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setTitle("Recording Rota");
			dialog.setDialogPane(pane);
			/*
			 * display dialog
			 */
			dialog.showAndWait();
			
		} catch (IOException e) {
			/*
			 * if there is an exception during fxml parsing, log an error message
			 * display an alert dialog to the user and exit program.
			 */
			logger.error("I/O exception occurred while loading recording dialog - {}", e.getMessage());
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(ErrorMessages.FATAL_ERROR);
			alert.setHeaderText(ErrorMessages.FORM_LOAD_ERROR);
			alert.setContentText("An exception occurred during the form loading process. See log file for details!");
			alert.showAndWait();
			Platform.exit();	
		}
	}

	/**
	 * Duplication Rota Button
	 * Event Handler
	 * 
	 * This method is called when the end user clicks the duplication rota button. The
	 * method validates the form and generates the diary using the domain model. The duplication
	 * rota has a dependency on the recording rota, using it to generate the dates for the week
	 * after recording. The results are displayed in a dialog box {@link DuplicationDiaryController}.
	 * 
	 * @param button click event
	 * 
	 * @since 1.0
	 */
	@FXML
	public void handleGenerateDuplicationDiaryButtonAction(ActionEvent event) {

		/*
		 * validate form
		 */
		if (!validateForm()) return;
		
		/*
		 * The duplication rota has a dependency on the recording rota.
		 * create recording rota model using builder pattern to use as a dependency
		 * when generating the duplication rota.
		 */
		var recordingDiary = new RecordingDiary.RecordingDiaryBuilder()
				.edition(Integer.valueOf(magazineEditionTextField.getText()))
				.januaryEdition(januaryEditionDatePicker.getValue())
				.compiler_1(compiler_1TextField.getText())
				.compiler_2(compiler_2TextField.getText())
				.build();
		
		/*
		 * must call this before generating the diary
		 */
		recordingDiary.setYear(this.currentYear);
		
		/*
		 * generate recording diary entries
		 */
		recordingDiary.generateDiary();
		
		/*
		 * create duplication rota model using builder pattern
		 */
		var duplicationDiary = new DuplicationDiary.DuplicationDiaryBuilder()
				.recordingDiary(recordingDiary)
				.build();
		
		/*
		 * must call this before generating the diary
		 */
		duplicationDiary.setYear(this.currentYear);
		
		/*
		 * generate duplication diary entries
		 */
		duplicationDiary.generateDiary();
		
		/*
		 * display dialog
		 */
		displayDuplicationDiaryDialog(duplicationDiary);
	}

	/**
	 * 
	 * Display Duplication Rota Dialog Method
	 * 
	 * This method displays the results of the rota generator which has created
	 * the duplication rota based on user inputs.
	 *  
	 * @param recording rota model
	 * @since 1.0
	 */
	private void displayDuplicationDiaryDialog(DuplicationDiary duplicationDiary) {
		/*
		 * display dialog
		 */
		try {
			/*
			 * create the loader & parse fxml file
			 */
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/duplication.fxml"));
			DialogPane pane = loader.load();
			pane.getButtonTypes().add(ButtonType.CLOSE);

			/*
			 * get controller reference and set properties
			 */
			DuplicationDiaryController controller = loader.getController();
			controller.setDiaryItems(duplicationDiary.getEntries());
			controller.setYear(duplicationDiary.getYear());

			/*
			 * create dialog wrapper and set title and pane root
			 */
			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setTitle("Barcoding & Duplication Rota");
			dialog.setDialogPane(pane);
			
			/*
			 * display dialog
			 */
			dialog.showAndWait();
		} catch (IOException e) {
			/*
			 * if there is an exception during fxml parsing, log an error message
			 * display an alert dialog to the user and exit program.
			 */
			logger.error("I/O exception occurred while loading duplication dialog - {}", e.getMessage());
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(ErrorMessages.FATAL_ERROR);
			alert.setHeaderText(ErrorMessages.FORM_LOAD_ERROR);
			alert.setContentText("An exception occurred during the form loading process. See log file for details!");
			alert.showAndWait();
			Platform.exit();	
		}
		
	}

	/**
	 * Bank Holidays Button
	 * Event Handler
	 * 
	 * This method is called when the end user clicks the bank holiday button. The
	 * method displays the bank holidays dialog, which allows the user to fetch bank holidays
	 * from the Internet, input them manually or load them from a file.
	 * 
	 * @param button click event
	 * 
	 * @since 1.0
	 */
	@FXML
	public void handleBankHolidaysButtonAction(ActionEvent event) {
		displayBankHolidayDialog();
	}

	/**
	 * 
	 * Display Bank Holiday Dialog Method
	 * 
	 * This dialog allows the user to source the bank holidays used when generating the rotas
	 * either from online, input manually or loaded from a file.

	 * @since 1.0
	 */
	private void displayBankHolidayDialog() {
		/*
		 * display dialog
		 */
		try {
			/*
			 * create the loader & parse fxml file
			 */
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/bank-holiday.fxml"));
			DialogPane pane = loader.load();
			pane.getButtonTypes().add(ButtonType.CLOSE);

			/*
			 * get controller reference and set properties
			 */
			BankHolidayDialogController controller = loader.getController();
			controller.setYear(this.currentYear);

			/*
			 * create dialog wrapper and set title and pane root
			 */
			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setTitle("Bank Holidays");
			dialog.setDialogPane(pane);
			
			/*
			 * display dialog
			 */
			dialog.showAndWait();
		} catch (IOException e) {
			/*
			 * if there is an exception during fxml parsing, log an error message
			 * display an alert dialog to the user and exit program.
			 */
			logger.error("I/O exception occurred while loading bank holiday dialog " + e.getMessage());
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(ErrorMessages.FATAL_ERROR);
			alert.setHeaderText(ErrorMessages.FORM_LOAD_ERROR);
			alert.setContentText("An exception occurred during the form loading process. See log file for details!");
			alert.showAndWait();
			Platform.exit();	
		}
		
	}

	/**
	 * Validate Form Method
	 * 
	 * This method validates user input on the JavaFX form. If a validation step fails the method returns false. If
	 * all the validation steps have passed the method returns true.
	 * 
	 * @return validation status false=failed, true=passed
	 */
	private boolean validateForm() {
		
		/*
		 * check year in bounds
		 */
		int year = diaryYearChoiceBox.getValue();
		if (year < 2000 || year > 2500) {
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(ErrorMessages.MAIN_FORM_ERROR);
			alert.setHeaderText(ErrorMessages.MAIN_FORM_VALIDATION_ERROR);
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
				alert.setTitle(ErrorMessages.MAIN_FORM_ERROR);
				alert.setHeaderText(ErrorMessages.MAIN_FORM_VALIDATION_ERROR);
				alert.setContentText(String.format("Edition should be greater than 0. You entered %d", edition));
				alert.showAndWait();
				return false;
			}
		} catch (NumberFormatException ex) {
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(ErrorMessages.MAIN_FORM_ERROR);
			alert.setHeaderText(ErrorMessages.MAIN_FORM_VALIDATION_ERROR);
			alert.setContentText("Edition field should be numeric");
			alert.showAndWait();
			return false;
		}

		/*
		 * check january edition date has been entered
		 */
		var date = januaryEditionDatePicker.getValue();
		if (date == null) {
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(ErrorMessages.MAIN_FORM_ERROR);
			alert.setHeaderText(ErrorMessages.MAIN_FORM_VALIDATION_ERROR);
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
			alert.setTitle(ErrorMessages.MAIN_FORM_ERROR);
			alert.setHeaderText(ErrorMessages.MAIN_FORM_VALIDATION_ERROR);
			alert.setContentText(String.format("The selected date %s should match selected year %d", formattedDate, year));
			alert.showAndWait();
			return false;
		}

		/*
		 * check selected date is in January
		 */
		if (date.getMonth() != Month.JANUARY) {
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(ErrorMessages.MAIN_FORM_ERROR);
			alert.setHeaderText(ErrorMessages.MAIN_FORM_VALIDATION_ERROR);
			alert.setContentText(String.format("The selected date %s should be in January not %s", formattedDate, date.getMonth()));
			alert.showAndWait();
			return false;
		}
		
		/*
		 * make sure january date falls on a Monday
		 */
		if (date.getDayOfWeek() != DayOfWeek.MONDAY) {
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(ErrorMessages.MAIN_FORM_ERROR);
			alert.setHeaderText(ErrorMessages.MAIN_FORM_VALIDATION_ERROR);
			alert.setContentText(String.format("The selected date %s should fall on a Monday. You entered a date that falls on a %s", formattedDate, date.getDayOfWeek()));
			alert.showAndWait();
			return false;
		}
		
		/*
		 * make sure january date is not a bank holiday
		 */
		if (BankHolidayService.isBankHoliday(date)) {
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(ErrorMessages.MAIN_FORM_ERROR);
			alert.setHeaderText(ErrorMessages.MAIN_FORM_VALIDATION_ERROR);
			alert.setContentText(String.format("The selected date is a bank holiday"));
			alert.showAndWait();
			return false;
		}

		return true;
	}
}
