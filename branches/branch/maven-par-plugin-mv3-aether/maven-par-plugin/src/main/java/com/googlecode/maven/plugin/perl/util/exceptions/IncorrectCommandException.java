/**
 * 
 */
package com.googlecode.maven.plugin.perl.util.exceptions;

/**
 * Exceptions raised in case the provided command is incorrect
 * 
 * @author marguerp Modified by $Author: marguerp $
 * @version $Revision: #2 $
 */
public class IncorrectCommandException extends CommandExecutionException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public IncorrectCommandException() {
    super();
  }

  /**
   * @param message
   */
  public IncorrectCommandException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public IncorrectCommandException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public IncorrectCommandException(String message, Throwable cause) {
    super(message, cause);
  }

}
