/**
 * 
 */
package com.googlecode.maven.plugin.perl.util.maven.exceptions;

/**
 * Exception raised if a file can no be copied
 * 
 * @author marguerp Modified by $Author: marguerp $
 * @version $Revision: #1 $
 */
public class UnableToCopyFileException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public UnableToCopyFileException() {
    super();
  }

  /**
   * @param message
   */
  public UnableToCopyFileException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public UnableToCopyFileException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public UnableToCopyFileException(String message, Throwable cause) {
    super(message, cause);
  }

}
