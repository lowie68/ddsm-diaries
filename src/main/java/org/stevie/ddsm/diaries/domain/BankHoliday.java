/**
 * Bank Holiday Class
 * 
 * This class is the object which is mapped to the JSON retrieved from the bank holiday REST API
 * 
 */
package org.stevie.ddsm.diaries.domain;

import java.time.LocalDate;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = BankHoliday.Builder.class)
public final class BankHoliday {
	
	private final LocalDate date;
	private final String localName;
	private final String name;
	private final String countryCode;
	private final boolean fixed;
	private final boolean global;
	private final String[] counties;
	private final String type;
	
	private BankHoliday(LocalDate date, String localName, String name, String countryCode, boolean fixed, boolean global,
			String[] counties, String type) {
		super();
		this.date = date;
		this.localName = localName;
		this.name = name;
		this.countryCode = countryCode;
		this.fixed = fixed;
		this.global = global;
		this.counties = counties;
		this.type = type;
	}
	
	public LocalDate getDate() {
		return date;
	}

	public String getLocalName() {
		return localName;
	}

	public String getName() {
		return name;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public boolean isFixed() {
		return fixed;
	}

	public boolean isGlobal() {
		return global;
	}

	public String[] getCounties() {
		return counties;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return String.format(
				"BankHoliday [date=%s, localName=%s, name=%s, countryCode=%s, fixed=%s, global=%s, counties=%s, type=%s]",
				date, localName, name, countryCode, fixed, global, Arrays.toString(counties), type);
	}

	@JsonPOJOBuilder
	static class Builder {
		private LocalDate date;
		private String localName;
		private String name;
		private String countryCode;
		private boolean fixed;
		private boolean global;
		private String[] counties;
		private String type;
		
		@JsonProperty("date")
		public Builder date(LocalDate date) {
			this.date = date;
			return this;
		}

		@JsonProperty("localName")
		public Builder localName(String localName) {
			this.localName = localName;
			return this;
		}

		@JsonProperty("name")
		public Builder name(String name) {
			this.name = name;
			return this;
		}

		@JsonProperty("countryCode")
		public Builder countryCode(String countryCode) {
			this.countryCode = countryCode;
			return this;
		}

		@JsonProperty("fixed")
		public Builder fixed(boolean fixed) {
			this.fixed = fixed;
			return this;
		}

		@JsonProperty("global")
		public Builder global(boolean global) {
			this.global = global;
			return this;
		}
		
		@JsonProperty("counties")
		public Builder counties(String[] counties) {
			this.counties = counties;
			return this;
		}

		@JsonProperty("type")
		public Builder type(String type) {
			this.type = type;
			return this;
		}

		public BankHoliday build() {
			return new BankHoliday(date, localName, name, countryCode, fixed, global, counties, type );
		}
	}
	
	
}
