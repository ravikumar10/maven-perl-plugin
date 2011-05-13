/**
 * 
 */
package com.googlecode.maven.plugin.perl.par.exceptions;

/**
 * @author pierre
 *
 */
public class IncorrectDestinationDirectoryException extends
		IllegalArgumentException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public IncorrectDestinationDirectoryException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public IncorrectDestinationDirectoryException(String message,
			Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param s
	 */
	public IncorrectDestinationDirectoryException(String s) {
		super(s);
	}

	/**
	 * @param cause
	 */
	public IncorrectDestinationDirectoryException(Throwable cause) {
		super(cause);
	}
	
	

}
