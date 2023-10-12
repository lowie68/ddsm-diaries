package org.stevie.ddsm.diaries.service.bank;

public final class CacheFailureException extends Exception {

	private final int year;
	
	public CacheFailureException(int year) {
		super("Could not find bank holidays in cache for specified year " + Integer.toString(year));
		this.year = year;
	} 
	
	public int getYear() {
		return this.year;
	}
}
