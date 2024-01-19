/**
 * <h3>Duplication Diary Class<h3>
 * 
 * <p>Duplication and dispatch of memory sticks is done the week following the recording. On the Tuesday 
 * the memory sticks are collected. On Wednesday the pouches are prepared for dispatch. On Thursday 
 * the memory sticks are copied from the master and stuffed into the pouches. 
 * The pouches are then posted to the clients. The recording diary is used as a dependency and 
 * must have been generated prior to generating the duplication diary.</p>
 * 
 * @author Stephen
 * @version 1.0
 */
package org.stevie.ddsm.diaries.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Duplication Diary Class
 * 
 * This class is part of the model. It is marked with the final keyword to prevent inheritance.
 * It uses the {@link RecordingDiary} as a basis for creating the {@link DuplicationDiary}
 * 
 */
public final class DuplicationDiary extends AbstractDiary<DuplicationDiaryEntry> {

	/*
	 * logger
	 */
	private static Logger logger = LoggerFactory.getLogger(DuplicationDiary.class);

	/*
	 * recording diary dependency
	 */
	private RecordingDiary recordingDiary;
	
	/*
	 * diary entries (12 per year)
	 */
	private List<DuplicationDiaryEntry> diaryEntries = new ArrayList<>();
	
	/**
	 * Copy Constructor
	 * 
	 * This constructor creates a duplication diary and generates the items in the diary.
	 * It is marked private to make sure that other classes can only create DuplicationDiary
	 * objects by using the builder pattern.  
	 * 
	 * @since 1.0
	 */
	private DuplicationDiary(DuplicationDiaryBuilder builder) {
		
		/*
		 * recording diary dependency 
		 */
		this.recordingDiary = builder.recordingDiary;
		
	}

	/**
	 * Generate Diary Method
	 * 
	 * This method generates all the entries for this years diary. It uses the recording
	 * diary as a basis for generating the duplication diary entries.
	 * 
	 * @since 1.0
	 */
	@Override
	public void generateDiary() {
		/*
		 * fetch recording diary entries
		 */
		var recordingEntries = recordingDiary.getEntries();
		
		/*
		 * Create duplication diary using JDK 8 lambdas
		 */
		diaryEntries = recordingEntries.stream().map(this::buildDuplicationEntry).toList();
	}
	
	/**
	 * Build An Entry Method
	 * 
	 * This method takes a recording diary entry and uses it to create a duplication diary
	 * entry. It is called using a method reference
	 * 
	 * @param recording diary entry
	 * @returns new duplication diary entry 
	 */
	private DuplicationDiaryEntry buildDuplicationEntry(RecordingDiaryEntry re) {
		Objects.requireNonNull(re);
		LocalDate nextTuesday = re.recordingDate().plusDays(8L);
		LocalDate nextWednesday = re.recordingDate().plusDays(9L);
		LocalDate nextThursday = re.recordingDate().plusDays(10L);
		return new DuplicationDiaryEntry(re.recordingDate().getMonth(), nextTuesday, nextWednesday, nextThursday);
	}

	/**
	 * Print Diary To Console Method
	 * 
	 * Print the diary entries to the console.
	 * 
	 * @since 1.0
	 */
	@Override
	public void printDiaryToConsole() {
		logger.info("DUPLICATION DATES {}", recordingDiary.getYear());
		for (DuplicationDiaryEntry entry : diaryEntries) {
			var sb = new StringBuilder();
			sb.append(entry.month()+ " ");
			sb.append("Tuesday=" + entry.collectDate().getDayOfMonth() + " ");
			sb.append("Wednesday=" + entry.barcodingDate().getDayOfMonth() + " ");
			sb.append("Thursday=" + entry.duplicationDate().getDayOfMonth());
			if (!sb.toString().isEmpty()) {
				var str = sb.toString();
				logger.info(str);
			}
		}
	}

	/**
	 * Get Entries
	 * 
	 * Returns all the entries in the diary. The method returns a defensive copy to preserve the 
	 * invariants of the class.
	 * 
	 * @return list of entries 12 in total 
	 * 
	 */
	@Override
	public List<DuplicationDiaryEntry> getEntries() {
		return new ArrayList<>(diaryEntries);
	}

	/**
	 * Builder Pattern
	 * 
	 * This class is nested class which is used as part of the builder pattern
	 * 
	 * @author Stephen
	 *
	 */
	public static class DuplicationDiaryBuilder {

		private RecordingDiary recordingDiary;
		
		public DuplicationDiaryBuilder recordingDiary(RecordingDiary recordingDiary) {
			this.recordingDiary = recordingDiary;
			return this;
		}

		public DuplicationDiary build() {
			return new DuplicationDiary(this);
		}

	}

}
