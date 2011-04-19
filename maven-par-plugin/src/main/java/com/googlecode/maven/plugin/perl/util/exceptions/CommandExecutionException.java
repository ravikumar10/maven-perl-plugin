/**
 * 
 */
package com.googlecode.maven.plugin.perl.util.exceptions;

/**
 * Exception raised if an error occurred during the command execution
 * 
 * @author marguerp Modified by $Author: marguerp $
 * @version $Revision: #2 $
 */
public class CommandExecutionException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public CommandExecutionException() {
   super();
  }

  /**
   * @param message
   */
  public CommandExecutionException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public CommandExecutionException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public CommandExecutionException(String message, Throwable cause) {
    super(message, cause);
  }

}
