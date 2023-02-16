/**
 * Recording Diary Entry
 * 
 * The recording diary has 12 entries one for each month. A record is used to reduce boiler plate code.
 * 
 */
package org.stevie.ddsm.diaries.domain;

import java.time.LocalDate;

public record RecordingDiaryEntry(LocalDate recordingDate, int edition, Team team, String compiler, String assistantCompiler) {

}
