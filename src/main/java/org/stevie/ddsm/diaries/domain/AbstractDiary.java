/**
 * <h3>Abstract Diary Class</h3>
 * 
 * <p>Contains functionality shared by the concrete diary classes {@link RecordingDiary} 
 * and {@link DuplicationDiary}</p>
 * 
 * @author Stephen
 * @version 1.0
 */
package org.stevie.ddsm.diaries.domain;

import java.util.List;

/**
 * Abstract Diary Class
 * 
 * Base class for diary subclasses uses new sealed keyword
 */
public abstract sealed class AbstractDiary<T> permits RecordingDiary, DuplicationDiary {

	/*
	 * diary year
	 */
	private int year;

	/**
	 * Year Getter Method
	 * 
	 * Every diary has a year
	 * 
	 * @return diary year
	 * @since 1.0
	 */
	public int getYear() {
		return year;
	}

	/**
	 * Year Setter Method
	 * 
	 * Must be called after object has been constructed
	 * 
	 * @param diary year
	 * @since 1.0
	 */
	public void setYear(int year) {
		this.year = year;
	}
	
	/**
	 * Get Entries Method
	 * 
	 * This method should return the diary entries. You must override this method to retrieve the 
	 * entries for the diary
	 *  
	 * @return generic list of diary entries
	 * @since 1.0
	 */
	public abstract List<T> getEntries();
	
	/**
	 * Print Diary To Console
	 * 
	 * This method should output the diary to the console
	 * 
	 * @since 1.0
	 * 
	 */
	public abstract void printDiaryToConsole();
	
	/**
	 * Generate Diary Method
	 * 
	 * You must call this method to generate diary entries. Make sure to set the year of the diary
	 * prior to calling this method.
	 * 
	 * @since 1.0
	 */
	public abstract void generateDiary();
}
