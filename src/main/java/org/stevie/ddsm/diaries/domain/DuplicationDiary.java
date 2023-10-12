/**
 * Duplication Diary Class
 * 
 * Duplication and dispatch of memory sticks is done the week following the recording. On the Tuesday 
 * the memory sticks are prepared. On Wednesday the pouches are prepared for dispatch. On the Thursday 
 * the memory sticks are copied from the master and stuffed into the pouches. 
 * The pouches are then posted to the clients. The recording diary is used as a dependency and 
 * must have been generated prior to generating the duplication diary.
 * 
 */
package org.stevie.ddsm.diaries.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DuplicationDiary implements DdsmDiary {

	private static Logger logger = LoggerFactory.getLogger(DuplicationDiary.class);

	/**
	 * Date Arithmetic
	 * 
	 * Calculates the first Tuesday of the week following the recording date.
	 */
	static TemporalAdjuster NEXT_WEEK_TUESDAY = TemporalAdjusters.ofDateAdjuster(date -> {
	    DayOfWeek dayOfWeek = date.getDayOfWeek();
	    if (dayOfWeek == DayOfWeek.MONDAY)
		    return date.plusDays(8L);
	    else
	        return date.plusDays(7L);
	});

	private RecordingDiary recordingDiary;
	private List<DuplicationDiaryEntry> diaryEntries = new ArrayList<>();
	private int diaryYear;

	private DuplicationDiary(DuplicationDiaryBuilder builder) {
		this.recordingDiary = builder.recordingDiary;
		this.diaryYear = this.recordingDiary.getDiaryYear();
		generateDiary();
	}

	/**
	 * Generate Diary
	 * Get all the recording diary entries. For each entry calculate the first Tuesday in the
	 * following week. This is  
	 */
	private void generateDiary() {
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
	 * Method Reference
	 * 
	 * Used as part of JDK 8 stream.
	 *  
	 */
	private DuplicationDiaryEntry buildDuplicationEntry(RecordingDiaryEntry re) {
		Objects.requireNonNull(re);
		LocalDate tuesday = re.recordingDate().with(NEXT_WEEK_TUESDAY);
		LocalDate wednesday = tuesday.plusDays(1L);
		LocalDate thursday = wednesday.plusDays(1L);
		return new DuplicationDiaryEntry(re.recordingDate().getMonth(), tuesday, wednesday, thursday);
	}

	/**
	 * Print Diary
	 * 
	 * Print the diary entries to the console.
	 * 
	 */
	@Override
	public void printDiaryToConsole() {
		logger.info("DUPLICATION DATES {}", recordingDiary.getDiaryYear());
		for (DuplicationDiaryEntry entry : diaryEntries) {
			var sb = new StringBuilder();
			sb.append(entry.month()+ " ");
			sb.append("Tuesday=" + entry.bookingInDate().getDayOfMonth() + " ");
			sb.append("Wednesday=" + entry.barcodingDate().getDayOfMonth() + " ");
			sb.append("Thursday=" + entry.duplicationDate().getDayOfMonth());
			if (!sb.toString().isEmpty()) {
				var str = sb.toString();
				logger.info(str);
			}
		}
	}

	/**
	 * Get Diary Year
	 * 
	 * Retrieve the year of this diary
	 * 
	 * @return year
	 */
	@Override
	public int getDiaryYear() {
		return this.diaryYear;
	}

	/**
	 * Get Entries
	 * 
	 * Returns all the entries in the diary. These are copied to an observable list for presentation in
	 * the UI. The method returns a defensive copy to preserve the invariants of the class.
	 * 
	 * @return list of entries 12 in total 
	 * 
	 */
	public List<DuplicationDiaryEntry> getEntries() {
		return new ArrayList<>(diaryEntries);
	}

	/**
	 * Builder Pattern
	 * 
	 * This class is nested class which is used as part of the builder pattern
	 * 
	 * @author steph
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
