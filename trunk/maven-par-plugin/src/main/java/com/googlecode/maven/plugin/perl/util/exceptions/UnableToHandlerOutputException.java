/**
 * 
 */
package com.googlecode.maven.plugin.perl.util.exceptions;

/**
 * If an error occurs during output handling
 * 
 * @author marguerp Modified by $Author: marguerp $
 * @version $Revision: #1 $
 */
public class UnableToHandlerOutputException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public UnableToHandlerOutputException() {
   super();
  }

  /**
   * @param message
   */
  public UnableToHandlerOutputException(String message) {
    super(message);
    
  }

  /**
   * @param cause
   */
  public UnableToHandlerOutputException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public UnableToHandlerOutputException(String message, Throwable cause) {
    super(message, cause);
  }

}
