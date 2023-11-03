/**
 * Recording Diary Entry
 * 
 * The recording diary has 12 entries one for each month. A record is used to reduce boiler plate code.
 * 
 */
package org.stevie.ddsm.diaries.domain;

import java.time.LocalDate;
import java.time.Month;

public record RecordingDiaryEntry(Month month, LocalDate recordingDate, int edition, String compiler) {

}
