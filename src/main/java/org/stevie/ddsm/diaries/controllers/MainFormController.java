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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.stevie.ddsm.diaries.domain.DuplicationDiary;
import org.stevie.ddsm.diaries.domain.RecordingDiary;
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

@Component
public class MainFormController  implements Initializable {
	
	public static final String DIARY_ERROR_TITLE = "Input Error";
	public static final String FATAL_ERROR = "Fatal Error";
	public static final String INPUT_ERROR_HEADER = "Validation Error";

	private static Logger logger = LoggerFactory.getLogger(MainFormController.class);

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

	/**
	 * Initialise Form
	 * 
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		configureYearChoiceBox();
		configureJanuaryDatePicker();
		configureMagazineEditionTextField();
		configureInternetStatusChecker();
	}

	private void configureMagazineEditionTextField() {
		magazineEditionTextField.setText("400");
		magazineEditionTextField.setTooltip(new Tooltip("The edition carried forward from December in the previous year"));
		
	}

	private void configureJanuaryDatePicker() {

		/*
		 * Initialise the date picker when the form is first displayed 
		 */
		januaryEditionDatePicker.setValue(LocalDate.of(diaryYearChoiceBox.getValue(), 1, 1));
		januaryEditionDatePicker.setTooltip(new Tooltip("Input the date of the first recording carried over from the previous year"));
		
	}

	private void configureYearChoiceBox() {

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

		diaryYearChoiceBox.setTooltip(new Tooltip("Please select a year from choice box"));
	}

	private void configureInternetStatusChecker() {
		logger.info("Configuring internet status checker");
		/*
		 * check internet background task
		 */
		var service = new InternetStatusService();
		service.setPeriod(Duration.seconds(30));
		
		service.setOnRunning(
			e -> {
				internetConnectionLabel.setText(InternetStatusService.IN_PROGRESS);
				internetConnectionLabel.setStyle("-fx-text-fill: orange;");
			});

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
						alert.setTitle(FATAL_ERROR);
						alert.setHeaderText("Illegal Program State");
						alert.setContentText("An unrecognised enum was passed to switch statement. See log file for details.");
						alert.showAndWait();
						Platform.exit();
					}
				}
			});

		service.setOnFailed(
			e -> {
				internetConnectionLabel.setText(InternetStatusService.INTERNET_STATUS_CHECK_FAILED);
				internetConnectionLabel.setStyle("-fx-text-fill: red;");
				logger.error("Internet status background thread failed with exception {}", service.getException().getMessage());
				var alert = new Alert(AlertType.ERROR);
				alert.setTitle(FATAL_ERROR);
				alert.setHeaderText("Illegal Program State");
				alert.setContentText("The internet status check background thread failed. See log file for details.");
				alert.showAndWait();
			});

		service.start();
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
		 * create recording diary using builder pattern
		 */
		var recordingDiary = new RecordingDiary.RecordingDiaryBuilder()
				.diaryYear(diaryYearChoiceBox.getValue())
				.edition(Integer.valueOf(magazineEditionTextField.getText()))
				.januaryEdition(januaryEditionDatePicker.getValue())
				.compiler_1(compiler_1TextField.getText())
				.compiler_2(compiler_2TextField.getText())
				.build();
		
		/*
		 * display dialog
		 */
		displayRecordingDiaryDialog(recordingDiary);
		
	}

	private void displayRecordingDiaryDialog(RecordingDiary recordingDiary) {
		/*
		 * display dialog
		 */
		try {
			logger.info("Loading recording dialog from fxml");
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
				.ifPresent(bt -> logger.info("Close recording dates dialog"));
		} catch (IOException e) {
			logger.error("I/O exception occurred while loading recording dialog - {}", e.getMessage());
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(FATAL_ERROR);
			alert.setHeaderText("Error Loading Form");
			alert.setContentText("An exception occurred during the form loading process. See log file for details!");
			alert.showAndWait();
			Platform.exit();	
		}
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
		 * create recording diary dependency using builder pattern
		 */
		var recordingDiary = new RecordingDiary.RecordingDiaryBuilder()
				.diaryYear(diaryYearChoiceBox.getValue())
				.edition(Integer.valueOf(magazineEditionTextField.getText()))
				.januaryEdition(januaryEditionDatePicker.getValue())
				.compiler_1(compiler_1TextField.getText())
				.compiler_2(compiler_2TextField.getText())
				.build();
		
		/*
		 * generate duplication diary using builder pattern
		 */
		var duplicationDiary = new DuplicationDiary.DuplicationDiaryBuilder()
				.recordingDiary(recordingDiary)
				.build();
		
		/*
		 * display dialog
		 */
		displayDuplicationDiaryDialog(duplicationDiary);
	}

	private void displayDuplicationDiaryDialog(DuplicationDiary duplicationDiary) {
		/*
		 * display dialog
		 */
		try {
			logger.info("Loading duplication dialog from fxml");
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/duplication.fxml"));
			DialogPane pane = loader.load();
			DuplicationDiaryController controller = loader.getController();
			controller.setDiaryItems(duplicationDiary.getEntries());
			controller.setYear(duplicationDiary.getDiaryYear());

			pane.getButtonTypes().add(ButtonType.CLOSE);
			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setTitle("Preparation & Duplication Dates");
			dialog.setDialogPane(pane);
			dialog.showAndWait()
				.filter(response -> response == ButtonType.CLOSE)
				.ifPresent(bt -> logger.info("Close duplication dates dialog"));
		} catch (IOException e) {
			logger.error("I/O exception occurred while loading duplication dialog - {}", e.getMessage());
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(FATAL_ERROR);
			alert.setHeaderText("Error Loading Form");
			alert.setContentText("An exception occurred during the form loading process. See log file for details!");
			alert.showAndWait();
			Platform.exit();	
		}
		
	}

	@FXML
	public void handleBankHolidaysButtonAction(ActionEvent event) {
		displayBankHolidayDialog();
	}
	
	private void displayBankHolidayDialog() {
		/*
		 * display dialog
		 */
		try {
			logger.info("Loading bank holiday dialog from fxml");
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/bank-holiday.fxml"));
			DialogPane pane = loader.load();
			pane.getButtonTypes().add(ButtonType.CLOSE);
			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.setTitle("Bank Holidays");
			dialog.setDialogPane(pane);
			dialog.showAndWait()
				.filter(response -> response == ButtonType.CLOSE)
				.ifPresent(bt -> logger.info("Close bank holidaydialog"));
		} catch (IOException e) {
			logger.error("I/O exception occurred while loading bank holiday dialog - {}", e.getMessage());
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(FATAL_ERROR);
			alert.setHeaderText("Error Loading Form");
			alert.setContentText("An exception occurred during the form loading process. See log file for details!");
			alert.showAndWait();
			Platform.exit();	
		}
		
	}

	/**
	 * Validate Form
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
			alert.setTitle(DIARY_ERROR_TITLE);
			alert.setHeaderText(INPUT_ERROR_HEADER);
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
				alert.setHeaderText(INPUT_ERROR_HEADER);
				alert.setContentText(String.format("Edition should be greater than 0. You entered %d", edition));
				alert.showAndWait();
				return false;
			}
		} catch (NumberFormatException ex) {
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(DIARY_ERROR_TITLE);
			alert.setHeaderText(INPUT_ERROR_HEADER);
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
			alert.setHeaderText(INPUT_ERROR_HEADER);
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
			alert.setHeaderText(INPUT_ERROR_HEADER);
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
			alert.setHeaderText(INPUT_ERROR_HEADER);
			alert.setContentText(String.format("The selected date %s should be in January not %s", formattedDate, date.getMonth()));
			alert.showAndWait();
			return false;
		}
		
		/*
		 * make sure january date falls on a Monday
		 */
		if (date.getDayOfWeek() != DayOfWeek.MONDAY) {
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(DIARY_ERROR_TITLE);
			alert.setHeaderText(INPUT_ERROR_HEADER);
			alert.setContentText(String.format("The selected date %s should fall on a Monday. You entered a date that falls on a %s", formattedDate, date.getDayOfWeek()));
			alert.showAndWait();
			return false;
		}
		
		/*
		 * make sure january date is not a bank holiday
		 */
		if (BankHolidayService.isBankHoliday(date)) {
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(DIARY_ERROR_TITLE);
			alert.setHeaderText(INPUT_ERROR_HEADER);
			alert.setContentText(String.format("The selected date is a bank holiday"));
			alert.showAndWait();
			return false;
		}

		return true;
	}
}
