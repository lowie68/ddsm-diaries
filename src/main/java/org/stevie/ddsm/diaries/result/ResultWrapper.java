/**
 * <h3>Result Wrapper Class</h3>
 * 
 * <p>A problem occurred when using boolean return values to show that an I/O exception has occurred.
 * Calling code had to check the return value to see if it was false for a failed I/O or true
 * if the I/O was successful. This conflicted with methods that had a result which should normally
 * be the primary value to be returned from the method. To get round these problems a result wrapper
 * class wraps the primary result as well as features to show whether the method call was successful or failed
 * with an exception. In which case the exception is also wrapped in the result.</p>
 * 
 * @author Stephen
 * @version 1.0
 */
package org.stevie.ddsm.diaries.result;

import java.util.Optional;

/**
 * Result Wrapper Class
 * 
 * This class wraps the result of a method along with its status succeeded or failed with exception.
 * 
 * @param <T> The type of the primary result
 */
public final class ResultWrapper<T> {
	/*
	 * primary result of the method
	 */
	private T result;
	/*
	 * thrown exception if method failed
	 */
	private Optional<Exception> exception;
	/*
	 * method succeeded or failed 
	 */
	private ResultStatus status;
	
	/**
	 * Copy Constructor
	 * 
	 * Create a new object
	 * 
	 * @param method result
	 * @since 1.0
	 * 
	 */
	public ResultWrapper(T result, ResultStatus status) {
		this.result = result;
		this.status = status;
	}

	/**
	 * Property Getters
	 */
	public T getResult() {
		return result;
	}

	public Optional<Exception> getException() {
		return exception;
	}

	public ResultStatus getStatus() {
		return status;
	}

	/**
	 * Property Setters
	 */
	public void setResult(T result) {
		this.result = result;
	}

	public void setException(Optional<Exception> exception) {
		this.exception = exception;
	}

	public void setStatus(ResultStatus status) {
		this.status = status;
	}

	/**
	 * To String Method
	 * 
	 * Convert object to more readable string
	 */
	@Override
	public String toString() {
		return String.format("ResultWrapper [result=%s, exception=%s, status=%s]", result, exception, status);
	}
}
