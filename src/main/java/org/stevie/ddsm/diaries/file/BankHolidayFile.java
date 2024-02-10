/**
 * <h3>Bank Holiday File Class</h3>
 * 
 * <p>This class in used conjunction with the {@link TextFile} base class provides
 * the functionality required for handling the bank holiday file which allows the user
 * to export and import bank holidays to or from a text file.</p>
 * 
 * @author Stephen
 * @version 1.0
 */
package org.stevie.ddsm.diaries.file;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.stevie.ddsm.diaries.result.ResultStatus;
import org.stevie.ddsm.diaries.result.ResultWrapper;
import org.stevie.ddsm.diaries.service.bank.BankHoliday;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

/**
 * Bank Holiday File Class
 * 
 * This class encapsulates the bank holiday file which is used when the user wants
 * to import/export the bank holidays to or from a text file.
 * 
 */
public final class BankHolidayFile extends TextFile<BankHoliday> {
	
	/*
	 * regular expression used when validating file line
	 */
	private final static String REGEX_1 = "^\\d\\d/\\d\\d/\\d\\d\\d\\d,\\s([A-Za-z\\s'])+$";
	/*
	 * regular expression used when extracting date part
	 */
	private final static String REGEX_2 = "\\d\\d/\\d\\d/\\d\\d\\d\\d";
	/*
	 * match whole lines
	 */
	private static Pattern linePattern = Pattern.compile(REGEX_1);
	/*
	 * extract the date part
	 */
	private static Pattern datePattern = Pattern.compile(REGEX_2);
	
	/**
	 * Static Factory Method
	 * 
	 * This method is used to create new objects
	 * 
	 * @param path to wrap
	 * @return new bank holiday file object
	 */
	public static BankHolidayFile of(Path path) {
		return new BankHolidayFile(path);
	}
	
	/**
	 * Copy Constructor
	 * 
	 * Constructs a new bank holiday file object. Marked as private in order to force users of the
	 * class to use the static factory method.
	 * 
	 * @param path to the bank holiday file
	 */
	private BankHolidayFile(Path filePath) {
		super(filePath);
	}

	/**
	 * Write File Method
	 * 
	 * writes the source list to the text file.
	 * 
	 * @param the list to write to the text file
	 * @return result wrapper {@link ResultWrapper}
	 * @since 1.0
	 * 
	 */
	@Override
	public ResultWrapper<Boolean> writeFile(List<BankHoliday> sourceList) {
		ResultWrapper<Boolean> result = new ResultWrapper<Boolean>(Boolean.TRUE, ResultStatus.SUCCESSFUL);
	    try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(this.getFilePath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW))) {
	    	/*
	    	 * write the file
	    	 */
	    	sourceList.stream().map(this::toFileLine).forEachOrdered(pw::println);
	    	/*
	    	 * return success
	    	 */
	    	result.setResult(Boolean.TRUE);
	    	result.setStatus(ResultStatus.SUCCESSFUL);
	    }
	    catch (FileAlreadyExistsException e) { //the file already exists
    		/*
    		 * display a dialog to confirm that the user wishes to overwrite
    		 * the file
    		 */
    		Alert confirm = new Alert(AlertType.CONFIRMATION);
    		confirm.setTitle("Save Bank Holiday File");
    		confirm.setHeaderText("File Already Exists");
    		confirm.setContentText("The file " + getFileName() + " already exists. Do you want to overwrite it?");
    		confirm.getButtonTypes().removeAll(ButtonType.OK, ButtonType.CANCEL);
    		confirm.getButtonTypes().addAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
    		Optional<ButtonType> option = confirm.showAndWait();
    		if (option.isPresent()) {
    			/*
    			 * if the user selected yes
    			 */
    			if (option.get().equals(ButtonType.YES)) {
    				/*
    				 * delete the file
    				 */
    				ResultWrapper<Boolean> deleteResult = deleteFile();
    				/*
    				 * if the delete was successful
    				 */
    				if (deleteResult.getStatus() == ResultStatus.SUCCESSFUL) { //delete was successful
    					/*
    					 * write the file again
    					 */
    					try (PrintWriter pw2 = new PrintWriter(Files.newBufferedWriter(this.getFilePath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
    						/*
    						 * list to file
    						 */
    						sourceList.stream().map(this::toFileLine).forEachOrdered(pw2::println);
    						/*
    						 * return success
    						 */
    				    	result.setResult(Boolean.TRUE);
    				    	result.setStatus(ResultStatus.SUCCESSFUL);
    					} catch (IOException e2) { //write failed
    				    	/*
    				    	 * return failure
    				    	 */
    				    	result.setResult(Boolean.FALSE);
    				    	result.setStatus(ResultStatus.FAILED_WITH_EXCEPTION);
    				    	result.setException(Optional.of(e2));
    					}
    				} else { //delete failed with an exception
    					if (deleteResult.getStatus() == ResultStatus.FAILED_WITH_EXCEPTION) {
    						/*
    						 * return failure
    						 */
    				    	result.setResult(Boolean.FALSE);
    				    	result.setStatus(ResultStatus.FAILED_WITH_EXCEPTION);
    				    	result.setException(deleteResult.getException());
    					}
    				}
    			} else { // the user selected no or cancel
    				if (option.get().equals(ButtonType.NO) || option.get().equals(ButtonType.CANCEL)) 
    					/*
    					 * drop everything and return to main dialog
    					 */
    					result.setStatus(ResultStatus.CANCELLED);
			    		result.setResult(Boolean.FALSE);
    			}
    		} 
	    }
	    catch (IOException e) {
	    	/*
	    	 * return failure
	    	 */
	    	result.setResult(Boolean.FALSE);
	    	result.setStatus(ResultStatus.FAILED_WITH_EXCEPTION);
	    	result.setException(Optional.of(e));
	    } 

	    /*
	     * return the wrapper
	     */
	    return result;
	}

	/**
	 * Read File Method
	 * 
	 * Reads the text file converting each line to an object
	 * 
	 * @return result wrapper {@link ResultWrapper}
	 * @since 1.0
	 */
	@Override
	public ResultWrapper<List<BankHoliday>> readFile() {
		
		ResultWrapper<List<BankHoliday>> result = new ResultWrapper<List<BankHoliday>>(new ArrayList<BankHoliday>(), ResultStatus.SUCCESSFUL);
		try {
			/*
			 * read the lines from the file
			 */
			var fileLines = Files.readAllLines(getFilePath());
			/*
			 * convert each line to a {@link BankHoliday} object and add
			 * to the list
			 */
			var lines = fileLines.stream().map(this::fromFileLine).toList();
			/*
			 * return success 
			 */
			result.setStatus(ResultStatus.SUCCESSFUL);
			result.setResult(lines);
		} catch (IOException e) { //fails with exception
			/*
			 * return failed
			 */
			result.setResult(new ArrayList<>());
			result.setStatus(ResultStatus.FAILED_WITH_EXCEPTION);
			result.setException(Optional.of(e));
		}
		
		return result;
	}

	/**
	 * To File Line Method
	 * 
	 * This method is used to convert the object to a string which can be written
	 * to the text file.
	 * 
	 * @param object to serialise
	 * @return string to write to the file
	 * @since 1.0
	 */
	@Override
	public String toFileLine(BankHoliday obj) {
		var sb = new StringBuilder();
		DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		String formattedDate = obj.getDate().format(df);
		sb.append(formattedDate);
		sb.append(", ");
		sb.append(obj.getLocalName());
		return sb.toString();
	}

	/**
	 * From File Line Method
	 * 
	 * This method is used to convert the string to an object that can be stored
	 * in the list. There should be no problems if the file has been validated first.
	 * 
	 * @param text file line
	 * @return de-serialised object
	 * @since 1.0
	 */
	@Override
	public BankHoliday fromFileLine(String line) {
		try (Scanner scanner = new Scanner(line)) 
		{
			scanner.useDelimiter(", ");
			String dateString = scanner.next();
			String bankHolidayDescription = scanner.next();
			DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			var bankHolidayDate = LocalDate.parse(dateString, df);
			var bh = new BankHoliday.Builder()
					.date(bankHolidayDate)
					.localName(bankHolidayDescription)
					.name(bankHolidayDescription)
	    			.counties(new String[] {"GB-ENG"})
					.build();
			return bh;
		}
	} 
	
	/**
	 * Validate File
	 * 
	 * This method validates the file selected by the user to be in the correct format. It processes
	 * each line in the file to see if it matches a regular expression. It returns true if the 
	 * file is valid and false if not. A valid line has the form of a date dd/mm/yyyy followed by a delimiter
	 * of (, ) followed by a text string containing the description of the bank holiday. This method is 
	 * related to the readFile method and should be called before the readFile method.
	 * 
	 * @return result wrapper {@link ResultWrapper}
	 * @since 1.0
	 */
	public ResultWrapper<FileValidationResult> validateFile() {
		
		ResultWrapper<FileValidationResult> result = new ResultWrapper<FileValidationResult>(FileValidationResult.FILE_VALID, ResultStatus.SUCCESSFUL);
		try {
			/*
			 * read the file
			 */
			var fileLines = Files.readAllLines(getFilePath());
			/*
			 * for each line in the file
			 */
			for (String line : fileLines) {
				
				/*
				 * check it against the regex pattern
				 */
				Matcher lineMatcher = linePattern.matcher(line);
				/*
				 * if the line does'nt match the pattern
				 */
				if (!lineMatcher.find()) {
					/*
					 * set method result to invalid and return immediately
					 */
					result.setResult(FileValidationResult.FILE_INVALID);
					result.setStatus(ResultStatus.SUCCESSFUL);
					return result;
				}
				/*
				 * detect invalid dates such as 12/25/1993
				 */
				try {
					/*
					 * extract the date part of the line
					 */
					Matcher dateMatcher = datePattern.matcher(line);
					/*
					 * if there is a match
					 */
					if (dateMatcher.find()) {
						/*
						* extract the date part
						*/
						var dateString = dateMatcher.group();
						DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
						/*
						 * parse the date
						 */
						LocalDate.parse(dateString, df);
					}
				} catch (DateTimeParseException e) { //if the date is invalid
					/*
					 * set method result to invalid and return immediately
					 */
					result.setResult(FileValidationResult.FILE_INVALID);
					result.setStatus(ResultStatus.SUCCESSFUL);
					result.setException(Optional.of(e));
					return result;
					
				}
			}
			/*
			 * if we reach this point the file is valid so return
			 * success
			 */
			result.setResult(FileValidationResult.FILE_VALID);
			result.setStatus(ResultStatus.SUCCESSFUL);
		} catch (IOException e) { //exception occurred
			/*
			 * return failure
			 */
			result.setResult(FileValidationResult.FILE_INVALID);
			result.setStatus(ResultStatus.FAILED_WITH_EXCEPTION);
			result.setException(Optional.of(e));
		}
		
		return result;
	}
	
	@Override
	public String toString() {
		return String.format("Bank Holiday File [path = %s]", getFilePath());
	
	}
	
}
