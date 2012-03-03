/**
 * 
 */
package com.googlecode.maven.plugin.perl.par.exceptions;

/**
 * @author pierre
 *
 */
public class IncorrectZipFileException extends IllegalArgumentException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public IncorrectZipFileException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public IncorrectZipFileException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param s
	 */
	public IncorrectZipFileException(String s) {
		super(s);
	}

	/**
	 * @param cause
	 */
	public IncorrectZipFileException(Throwable cause) {
		super(cause);
	}

}
