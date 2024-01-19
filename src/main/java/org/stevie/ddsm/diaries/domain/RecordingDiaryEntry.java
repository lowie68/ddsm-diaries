/**
 * <h3>Recording Diary Entry Record</h3>
 * 
 * <p>The recording diary has one entry for each month (12 in total). The entry is usually
 * the first Monday in the month eacept for bank holidays.
 * one for each month. The record keyword is used to reduce boiler plate code and make the class 
 * immutable.</p>
 * 
 * @author Stephen
 * @version 1.0
 */
package org.stevie.ddsm.diaries.domain;

import java.time.LocalDate;
import java.time.Month;

/**
 * Recording Diary Entry Record 
 */
public record RecordingDiaryEntry(Month month, LocalDate recordingDate, int edition, String compiler) {

}
