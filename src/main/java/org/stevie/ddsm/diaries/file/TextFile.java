/**
 * <h3>Text File Base Class</h3>
 * 
 * <p>This class is a base class which encapsulates a text file. The path to the file
 * is set when the object is constructed. After that it cannot be changed. This class
 * should not be instantiated.</p>
 *  
 * @author Stephen
 * @version 1.0
 */
package org.stevie.ddsm.diaries.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.stevie.ddsm.diaries.result.ResultStatus;
import org.stevie.ddsm.diaries.result.ResultWrapper;
import org.stevie.ddsm.diaries.service.bank.BankHoliday;

/**
 * This class is an abstract class which represents a text file. The only 
 * permitted sub class is the {@link BankHolidayFile}. Used to store
 * bank holidays details. This functionality mainly keeps the I/O logic
 * in a separate class to avoids it polluting the UI code.
 *  
 * @param <T> type of the objects each line is going to be de-serialised to
 */
public abstract sealed class TextFile<T> permits BankHolidayFile {

	/*
	 * The path of the text file
	 */
	private Path filePath;
	
	/**
	 * Copy Constructor
	 * 
	 * The path to the file is set when instantiating the class
	 * 
	 * @param path to the text file
	 * @since 1.0
	 */
	public TextFile(Path filePath) {
		this.filePath = filePath;
	}
	
	/**
	 * Delete File Method
	 * 
	 * This method deletes the file. 
	 * 
	 * @return result wrapper {@link ResultWrapper}
	 * @since 1.0
	 */
	public ResultWrapper<Boolean> deleteFile() {
		ResultWrapper<Boolean> result = new ResultWrapper<Boolean>(Boolean.TRUE, ResultStatus.SUCCESSFUL);
		try {
			Files.delete(this.filePath);
			result.setResult(Boolean.TRUE);
			result.setStatus(ResultStatus.SUCCESSFUL);
		} catch (IOException e) {
			result.setResult(Boolean.FALSE);
			result.setStatus(ResultStatus.FAILED_WITH_EXCEPTION);
		}
		return result;
	}
	
	/**
	 * Get File Path Method
	 * 
	 * Getter method for the path property.
	 * 
	 * @return file path
	 * @since 1.0
	 */
	public Path getFilePath() {
		return this.filePath;
	}
	
	/**
	 * Get File Name Method
	 * 
	 * This method retrieves the file name part of the path.
	 * 
	 * @return file name
	 * @since 1.0
	 */
	public String getFileName() {
		return this.filePath.getFileName().toString();
	}

    /**
     * Get File Extension Method
     * 
     * This method retrieves the file extension of any path. It appears that the Files
     * class had a method which did this but now appears to have been deprecated.
     * 
     * @return the file extension string (not including . symbol)
     * @since 1.0
     */
    public Optional<String> getFileExtension() {
    	/*
    	 * retrieve the file extension by finding the . in the file name. If no extension
    	 * is found, return an empty optional.
    	 */
        return Optional.ofNullable(getFileName())
        	      .filter(f -> f.contains("."))
        	      .map(f -> f.substring(getFileName().lastIndexOf(".") + 1));
	}
    
    /**
     * Is Text File Method
     * 
     * Returns true if the file is a text file with an extension of txt. The program only deals with
     * text files for reading and writing bank holiday files.
     * 
     * @return true if extension is txt
     * @since 1.0
     * 
     */
    public boolean isTextFile() {
    	/*
    	 * get the file extension
    	 */
    	Optional<String> fileExtensionOptional = getFileExtension();
    	/*
    	 * if no extension is available return false
    	 */
    	if (fileExtensionOptional.isEmpty()) return false;
    	/*
    	 * retrieve the string from the optional
    	 */
    	String ext = fileExtensionOptional.get();
    	/*
    	 * return true if file extension is txt
    	 */
    	return ext.equals("txt");
    }

	/**
	 * To File Line
	 * 
	 * Converts the object to a string that can be written to the text file. Must be
	 * implemented by any sub-clases. Used when writing the file.
	 * 
	 * @param object to be converted
	 * @return string representation
	 * @since 1.0
	 */
	public abstract String toFileLine(T obj);
	
	/**
	 * From File Line
	 * 
	 * Converts the string read from the text file to an object. Must be implemented
	 * by any sub-classes. Used when reading the file.
	 * 
	 * @param line from text file
	 * @return converted object
	 * @since 1.0
	 */
	public abstract T fromFileLine(String line);
	
	/**
	 * Write File Method
	 * 
	 * Writes the input list to the text file. Uses the toFileLine to serialise the objects.
	 * Must be implemented by any sub-classes.
	 * 
	 * @param list to write to the text file
	 * @return true if file written successfully
	 * @since 1.0
	 */
	public abstract ResultWrapper<Boolean> writeFile(List<T> sourceList);
	
	/**
	 * Read File Method
	 * 
	 * Reads the text file converting the lines to objects using the fromFileLine to de-serialise
	 * the objects. Must be implemented by any sub-classes.
	 * 
	 * @return the text file as a list of objects
	 * @since 1.0
	 */
	public abstract ResultWrapper<List<BankHoliday>> readFile();

}
