/**
 * <h3>Bank Holiday Dialog Controller</h3>
 * 
 * <p>This class is an MVC Controller for the bank holiday dialog. The user can choose to download
 * the bank holidays online to retrieve the bank holidays for a particular year. If no Internet
 * is available, the user can choose to input them manually or import them from a text file.
 * 
 * @author Stephen
 * @version 1.0
 * 
 */
package org.stevie.ddsm.diaries.controllers;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.stevie.ddsm.diaries.file.BankHolidayFile;
import org.stevie.ddsm.diaries.file.FileValidationResult;
import org.stevie.ddsm.diaries.file.OpenFileChooser;
import org.stevie.ddsm.diaries.file.SaveFileChooser;
import org.stevie.ddsm.diaries.messages.ErrorMessages;
import org.stevie.ddsm.diaries.result.ResultStatus;
import org.stevie.ddsm.diaries.result.ResultWrapper;
import org.stevie.ddsm.diaries.service.bank.BankHoliday;
import org.stevie.ddsm.diaries.service.bank.BankHolidayCache;
import org.stevie.ddsm.diaries.service.bank.FetchBankHolidaysTask;
import org.stevie.ddsm.diaries.service.internet.InternetService;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Bank Holiday Dialog Class
 * 
 * JavaFX controller backing the view in bank-holiday.fxml.
 * 
 */
@Component
public class BankHolidayDialogController implements Initializable {
	
	/*
	 * logger
	 */
	private static Logger logger = LoggerFactory.getLogger(BankHolidayDialogController.class);
	
	/*
	 * JavaFX controls
	 */
	@FXML
	private Button findOnlineButton;
	@FXML
	private Button manualInputButton;
	@FXML
	private Button importFromFileButton;
	@FXML
	private Button exportToFileButton;
	@FXML
	private Button saveToCacheButton;
	@FXML
	private Label headerLabel;
	@FXML
	private TableView<BankHoliday> bankHolidayTableView;
    @FXML
    private TableColumn<BankHoliday, String> bankHolidayDateColumn;
    @FXML
    private TableColumn<BankHoliday, String> bankHolidayDescriptionColumn;
    
    /*
     * the year the dialog is related to
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
		
		/*
		 * configure the table view control
		 */
		configureTableView();
		
		
	}

	/**
	 * Configure Table View Method
	 * 
	 * The table view displays all the bank holidays in a given year. The user can download them from
	 * online, input them manually or import them from a text file.
	 * 
	 * @since 1.0
	 */
	private void configureTableView() {
		
		/*
		 * the date column displays the date of the bank holiday including the day that the bank holiday
		 * falls on. The cell value factory needs to be set to specify how to populate all
		 * cells within the date column.
		 */
		bankHolidayDateColumn.setCellValueFactory(bh -> {
			var date = bh.getValue().getDate();
			var formatter = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");
			var dateString = formatter.format(date);
			return new SimpleStringProperty(dateString);
		});	

		/*
		 * the bank holiday description column is a description of the bank holiday e.g. Christmas Day or
		 * May Bank Holiday etc.
		 */
		bankHolidayDescriptionColumn.setCellValueFactory(bh -> new SimpleStringProperty(bh.getValue().getLocalName()));

		/*
		 * make sure no bank holidays are displayed when the dialog is first loaded.
		 */
		bankHolidayTableView.setItems(FXCollections.observableArrayList());

	}

	/**
	 * Find Bank Holidays Online Button
	 * Event Handler
	 * 
	 * This method allows the user to download bank holidays using the nager date REST API.
	 * It first checks that an Internet connection is available. If so, it starts a background task
	 * which fetches the dates over the network. On task completion the retrieved dates are 
	 * stored in the bank holiday cache for subsequent use during rota generation.
	 * 
	 * @param button click event 
	 * @throws IOException if Internet status check fails
	 * 
	 * @since 1.0
	 */
	@FXML
	public void handleFindOnlineButtonAction(ActionEvent event) throws IOException {
		
		/*
		 * check Internet is available
		 */
		if (!InternetService.isInternetAvailable()) {
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle("Find Bank Holidays");
			alert.setHeaderText("No Internet Available");
			alert.setContentText("Unable to retrieve bank holidays from online API. Please input them manually.");
			alert.showAndWait();
			return;
		}
		
		/*
		 * create background task
		 */
		FetchBankHolidaysTask task = new FetchBankHolidaysTask(this.currentYear);
		
		/*
		 * The onSucceeded event handler is called whenever the Task state
    	 * transitions to the SUCCEEDED state. It calls the get method which
    	 * blocks until the result is available. If the get method times out after waiting 10 seconds
    	 * a {@code TimeoutException} is raised and an error message is output to the log.
    	 * If the get fails with an exception an {@code ExecutionException} is raised and an error
    	 * is output to the log. Once the task has completed successfully the underlying data model 
    	 * for the TableView is set to the list. 
		 */
		task.setOnSucceeded(ev -> {
			try {
				List<BankHoliday> list = task.get(10, TimeUnit.SECONDS);
				var bankHolidayItems = FXCollections.observableArrayList(list);
				bankHolidayTableView.setItems(bankHolidayItems);
			} catch (InterruptedException e) {
				logger.error("Bank holiday task cancelled while trying to retrieve bank holidays for year {}", this.currentYear);
				Thread.currentThread().interrupt();
			} catch (ExecutionException e) {
				logger.error("Bank holiday task failed while trying to retrieve bank holidays with exception {}", e.getCause());
			} catch (TimeoutException e) {
				logger.error("Bank holiday task timed out");
			}
			
		});
		
		/*
		 * The onFailed event handler is called whenever the Task state
    	 * transitions to the FAILED state. 
		 */
		task.setOnFailed(ev -> {
			logger.error("Bank holiday task failed while trying to retrieve bank holidays for year {} ", this.currentYear);
		});
		/*
		 * submit the task for execution
		 */
		try (ExecutorService executor = Executors.newCachedThreadPool()) {
			executor.submit(task);
		}
		
	}
	
	/**
	 * Manual Input Bank Holidays Button
	 * Action Event Handler
	 * 
	 * This event handler is invoked when the end user clicks the manual input dialog button.
	 * The dialog pane is loaded from the fxml file and is wrapped in a Dialog instance. The dialog
	 * has an Apply and Cancel button. It returns a result of {@code ObservableList<BankHoliday>} which 
	 * contains the bank holidays input by the user. </p>
	 * 
	 * @param button click event
	 * 
	 * @since 1.0
	 */
	@FXML
	public void handleManualInputButtonAction(ActionEvent event) {
		/*
		 * display manual input dialog
		 */
		try {
			/*
			 * create the loader & parse fxml file
			 */
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/manual-input.fxml"));
			DialogPane pane = loader.load();
			/*
			 * add dialog buttons 
			 */
			pane.getButtonTypes().add(ButtonType.CANCEL);
			pane.getButtonTypes().add(ButtonType.APPLY);
			/*
			 * get controller reference & set year parameter
			 */
			ManualInputDialogController controller = loader.getController();
			controller.setYear(this.currentYear);
			/*
			 * create dialog instance, set window title and pane root
			 */
			Dialog<ObservableList<BankHoliday>> dialog = new Dialog<>();
			dialog.setTitle("Bank Holidays Manual Input");
			dialog.setDialogPane(pane);
			/*
			 * The dialog returns a result of {@code ObservableList<BankHoliday>}. If the 
			 * {@code ButtonType.APPLY} button was clicked.
			 */
			dialog.setResultConverter(new Callback<ButtonType, ObservableList<BankHoliday>>() {
			    @Override
			    public ObservableList<BankHoliday> call(ButtonType b) {
			        if (b == ButtonType.APPLY) {
			        	return controller.getBankHolidayItems();
			        }

			        return null;
			    }
			});
			
			/*
			 * This will block until the dialog is closed.  
			 */
			Optional<ObservableList<BankHoliday>> result = dialog.showAndWait();
			/*
			 * if the optional contains a value, the underlying data model for the TableView 
			 * is set to the list
			 */
			if (result.isPresent()) {
				var bankHolidayList = result.get();
				bankHolidayTableView.setItems(bankHolidayList);
			}			
		/*
		 * if an {@code IOException} is raised during the form loading process an error message
		 * is output to the log and an error box is presented to the user. The program then exits.	
		 */
		} catch (IOException e) {
			logger.error("I/O exception occurred while loading manual input dialog - {}",  e);
			var alert = new Alert(AlertType.ERROR);
			alert.setTitle(ErrorMessages.FATAL_ERROR);
			alert.setHeaderText(ErrorMessages.FORM_LOAD_ERROR);
			alert.setContentText("An exception occurred during the form loading process. See log file for details!");
			alert.showAndWait();
			Platform.exit();	
		}
	}

	/**
	 * Import From File Button
	 * Event Handler
	 * 
	 * This method imports bank holidays from a text file. It uses
	 * the standard open file dialog using the {@link FileChooser} abstraction.
	 * 
	 * @param button click event
	 * @since 1.0
	 */
    @FXML
    void handleImportFromFileButtonAction(ActionEvent event) {
    	/*
    	 * display the dialog
    	 */
    	Optional<Path> optionalPath = displayOpenFileChooser();
    	/*
    	 * if the user cancelled the dialog drop everything and return
    	 */
    	if (optionalPath.isEmpty()) return;
    	/*
    	 * get the path from the optional
    	 */
    	Path path = optionalPath.get();
    	/*
    	 * create a bank holiday file wrapper
    	 */
    	BankHolidayFile bankHolidayFile = BankHolidayFile.of(path);
    	/*
    	 * check file is a text file
    	 */
    	if (!bankHolidayFile.isTextFile()) {
    		/*
    		 * if the selected file is not a text file display a warning message
    		 * drop everything and return.
    		 */
			var alert = new Alert(AlertType.WARNING);
			alert.setTitle("Import File");
			alert.setHeaderText("File Extension Warning");
			alert.setContentText("Please make sure the file you choose is a text file (*.txt)");
			alert.showAndWait();
			return;
    	}     	
    	/*
    	 * make sure the file is formatted correctly
    	 */
    	ResultWrapper<FileValidationResult> validateResult = bankHolidayFile.validateFile();
     	if (validateResult.getStatus() == ResultStatus.SUCCESSFUL) {
    		/*
    		 * no exceptions occurred so check the result
    		 */
    		if (validateResult.getResult() == FileValidationResult.FILE_INVALID) {
        		/*
        		 * validation of the file failed so display an error message to the user then
        		 * drop everything and return
        		 */
    			var alert = new Alert(AlertType.ERROR);
    			alert.setTitle("Import File");
    			alert.setHeaderText("File Validation Error");
    			alert.setContentText("Please make sure the file has the correct format. Please alter the file and re-try.");
    			alert.showAndWait();
    			return;
    		}
    	} else { //there was an exception
    		if (validateResult.getStatus() == ResultStatus.FAILED_WITH_EXCEPTION) {
    			logger.error("An exception occured while trying to validate the file {} {}", bankHolidayFile.getFilePath(), validateResult.getException().get());
            	fatalError("An exception occurred while trying to validate the file " + bankHolidayFile.getFileName() + ". See log file for details!");
    		}
    	}	
       	/*
    	 * read the file into a list
    	 */
    	ResultWrapper<List<BankHoliday>> readResult = bankHolidayFile.readFile();
    	/*
    	 * if the method was successful
    	 */
    	if (readResult.getStatus() == ResultStatus.SUCCESSFUL) {
    		/*
    		 * get the result from the wrapper
    		 */
    		var bankHolidays = readResult.getResult();
        	/*
        	 * update the table view model
        	 */
        	var observableList = FXCollections.observableArrayList(bankHolidays);
        	bankHolidayTableView.setItems(observableList);
    		
    	}
    	else { //the method had an exception
    		if (readResult.getStatus() == ResultStatus.FAILED_WITH_EXCEPTION) {
       			logger.error("An exception occured while trying to read the file {} {}", bankHolidayFile.getFilePath(), validateResult.getException().get());
            	fatalError("An exception occurred while trying to read the file " + bankHolidayFile.getFileName() + ". See log file for details!");
    		}
    	}
    }

    /**
     * Display Open File Chooser
     * 
     * This method is small chunk of the file import facility. It displays the standard
     * open file dialog {@link OpenFileChooser} and retrieves the file to read. If the user
     * did'nt select a file in the dialog the optional should be empty.
     * 
     * @return selected file path (wrapped in an optional instead of null)
     * @since 1.0
     */
	private Optional<Path> displayOpenFileChooser() {
        /*
         * get the parent stage
         */
        var stage = (Stage) importFromFileButton.getScene().getWindow();
        /*
    	 * instantiate the save file dialog
    	 */
    	var openChooser = new OpenFileChooser(stage);
    	/*
    	 * open the dialog
    	 */
    	Optional<Path> optionalPath = openChooser.showChooser();
		return optionalPath;
	}

	/**
     * Export To File Button
     * Event Handler
     * 
     * This method exports the bank holidays in the {@link TableView} to a text file. It uses the
     * standard save file dialog using the {@link FileChooser} abstraction.
     *  
     * @param button click event
     * @since 1.0
     */
    @FXML
	void handleExportToFileButtonAction(ActionEvent event) {
    	/*
    	 * make sure there are items in the list to write to the file.
    	 * If there are no bank holidays, alert the user and return
    	 */
    	if (bankHolidayTableView.getItems().size() <= 0) {
    		var info = new Alert(AlertType.INFORMATION);
    		info.setTitle("Export Bank Holidays");
    		info.setHeaderText("Export Bank Holidays");
    		info.setContentText("There are no bank holidays to export");
    		info.showAndWait();
    		return;
    	}
    	/*
    	 * display the file chooser
    	 */
    	Optional<Path> optionalPath = displaySaveFileChooser();
    	/*
    	 * if the user cancelled the dialog drop everything and return
    	 */
    	if (optionalPath.isEmpty()) return;
    	/*
    	 * retrieve the path
    	 */
    	Path path = optionalPath.get();
    	/*
    	 * create the bank holiday file wrapper 
    	 */
        BankHolidayFile bankHolidayFile = BankHolidayFile.of(path);
        /*
    	 * write all the bank holidays to the file. 
    	 */
        ResultWrapper<Boolean> writeResult = bankHolidayFile.writeFile(bankHolidayTableView.getItems());
    	if (writeResult.getStatus() == ResultStatus.SUCCESSFUL) {
        	/*
        	 * display a successful dialog to the user
        	 */
        	var info = new Alert(AlertType.INFORMATION);
        	info.setTitle("Export Bank Holidays");
        	info.setHeaderText("Export Bank Holidays");
        	info.setContentText("File Written Successfully!");
        	info.showAndWait();
    	} else { //something went wrong during the write
    		if (writeResult.getStatus() == ResultStatus.FAILED_WITH_EXCEPTION) {
        		/*
        		 * if the write failed display an error message and exit
        		 * program
        		 */
    			logger.error("An I/O exception occured while trying to write the file {} {}", bankHolidayFile.getFilePath(), writeResult.getException().get());
        		fatalError("An exception occurred while trying to write " + bankHolidayFile.getFileName() + ". See log file for details!");
    			
    		}
    	}
	}
    
    /**
     * Display Save File Chooser
     * 
     * This method is small chunk of the file export facility. It displays the standard
     * save file dialog {@link OSaveFileChooser} and retrieves the file to write. If the user
     * did'nt select a file in the dialog the optional should be empty.
     * 
     * @return selected file path (wrapped in an optional instead of null)
     * @since 1.0
     */
	private Optional<Path> displaySaveFileChooser() {
        /*
         * get the parent stage
         */
        var stage = (Stage) exportToFileButton.getScene().getWindow();
        /*
         * default file name of file to be saved
         */
        var fileName = String.format("bank holidays %d", this.currentYear);
        /*
    	 * instantiate the save file dialog
    	 */
    	var saveChooser = new SaveFileChooser(stage, fileName );
    	/*
    	 * open the dialog
    	 */
    	Optional<Path> optionalPath = saveChooser.showChooser();
		return optionalPath;
	}

	/**
     * Save To Cache Button
     * Event Handler
     * 
     * This method writes the bank holidays in the table view to the cache where they can be used
     * when checking if a given date is a bank holiday.
     *  
     * @param button click event
     * @since 1.0
     */
    @FXML
	void handleSaveToCacheButtonAction(ActionEvent event) {
    	/*
    	 * retrieve table view model
    	 */
    	var list = bankHolidayTableView.getItems();
    	/*
    	 * if there are no entries in the table
    	 */
    	if (list.size() <= 0) {
    		/*
    		 * display an message to the user and quit method
    		 */
    		var info = new Alert(AlertType.ERROR);
    		info.setTitle("Save To Cache");
    		info.setHeaderText("Write Bank Holidays To Cache");
    		info.setContentText("There are no entries in the table to write to the cache");
    		info.showAndWait();
    		return;
    		
    	}
    	/*
    	 * put the bank holidays in the memory cache for use by
    	 * the rest of the program
    	 */
		BankHolidayCache.putBankHolidaysInCache(list, this.currentYear);
    	/*
    	 * display a successful dialog to the user
    	 */
    	var info = new Alert(AlertType.INFORMATION);
    	info.setTitle("Save To Cache");
    	info.setHeaderText("Write Bank Holidays To Cache");
    	info.setContentText("Cache Updated Successfully!");
    	info.showAndWait();
    }
    
	/**
     * Fatal Error Method
     * 
     * display an error message to the user and exit program.
     * 
     * @param content text
     * @since 1.0
     */
	private void fatalError(String text) {
		var alert = new Alert(AlertType.ERROR);
		alert.setTitle(ErrorMessages.FATAL_ERROR);
		alert.setHeaderText(ErrorMessages.IO_ERROR);
		alert.setContentText(text);
		alert.showAndWait();
		Platform.exit();
	}

	/**
	 * Set Year Method
	 * 
	 * This method is called after the dialog has been constructed. It updates an instance field
	 * which is used elsewhere in the class. The view contains a {@code Label} which displays the 
	 * active year to the end user.
	 *  
	 * @param active year
	 * @since 1.0
	 */
	public void setYear(int year) {
		this.currentYear = year;
		headerLabel.setText("Bank Holidays For Year " + this.currentYear);
	}
}

