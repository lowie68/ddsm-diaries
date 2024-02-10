/**
 * <h3>Bank Holiday Class</h3>
 * 
 * <p>The bank holiday API sends the bank holidays for a given year over the Internet in JSON
 * format. The Jackson library converts the json string into an object. The class is annotated
 * with Jackson annotations which the library uses to deserialise the json string to a POJO. The class
 * is immutable once it is constructed.</p>
 * 
 */
package org.stevie.ddsm.diaries.service.bank;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Bank Holiday Class
 * 
 * Model object. (see the nager date API)
 */
@JsonDeserialize(builder = BankHoliday.Builder.class) //use the builder pattern to create an object of the bank holiday class
public final class BankHoliday {
	
	private final LocalDate date;
	private final String localName;
	private final String name;
	private final String countryCode;
	private final boolean fixed;
	private final boolean global;
	private final String[] counties;
	private final String type;
	
	/**
	 * Copy Constructor
	 * 
	 * Private constructor restricts class users to using the builder pattern
	 * 
	 * @param date of bank holiday
	 * @param local name description of the bank holiday
	 * @param name
	 * @param countryCode
	 * @param fixed
	 * @param global
	 * @param counties
	 * @param type
	 */
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
	
	/**
	 * property getter methods
	 */
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

	/**
	 * Hash Code Method
	 * 
	 * Used when storing objects in a HashMap
	 * 
	 * @since 1.0
	 */
	@Override
	public int hashCode() {
		return Objects.hash(date);
	}

	/**
	 * Equals Method
	 * 
	 * Two Bank Holiday objects are equal if the date is the same.
	 * 
	 * @param other object to compare with
	 * @return true if objects are equal
	 * @since 1.0
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BankHoliday other = (BankHoliday) obj;
		return Objects.equals(date, other.date);
	}

	/**
	 * To String Method
	 * 
	 * Returns a string representation of the object which is used when printing
	 * 
	 * @return string representation of the object
	 * @since 1.0
	 */
	@Override
	public String toString() {
		return String.format(
				"BankHoliday [date=%s, localName=%s, name=%s, countryCode=%s, fixed=%s, global=%s, counties=%s, type=%s]",
				date, localName, name, countryCode, fixed, global, Arrays.toString(counties), type);
	}
	
	/**
	 * Builder Class
	 * 
	 * This class is part of the builder pattern which is used to create new objects rather
	 * than using a constructor or static factory method.
	 * 
	 * @author Stephen
	 * @version 1.0
	 */
	@JsonPOJOBuilder
	public static class Builder {
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
