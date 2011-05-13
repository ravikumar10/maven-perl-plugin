/**
 * 
 */
package com.googlecode.maven.plugin.perl.util.exceptions;

/**
 * @author pierre
 *
 */
public class NonExistingProgramException extends CommandExecutionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public NonExistingProgramException() {
	  super();
	}

	/**
	 * @param message
	 */
	public NonExistingProgramException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public NonExistingProgramException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NonExistingProgramException(String message, Throwable cause) {
		super(message, cause);
	}

}
