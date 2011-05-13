/**
 * 
 */
package com.googlecode.maven.plugin.perl.par.exceptions;

/**
 * @author pierre
 *
 */
public class UnableToUnzipException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public UnableToUnzipException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnableToUnzipException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public UnableToUnzipException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public UnableToUnzipException(Throwable cause) {
		super(cause);
	}	
	
}
