/**
 * 
 */
package com.googlecode.maven.plugin.perl.util.exceptions;

/**
 * Exception raised in case the command failed and returned an error code
 * 
 * @author marguerp Modified by $Author: marguerp $
 * @version $Revision: #3 $
 */
public class ErrorCodeCommandExecutionException extends CommandExecutionException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L ;

  /**
   * 
   */
  public ErrorCodeCommandExecutionException(int returnvalue) {
    super(String.valueOf(returnvalue));
  }

  /**
   * @param message
   */
  public ErrorCodeCommandExecutionException(String message) {
    super(message);
    
  }

  /**
   * @param cause
   */
  public ErrorCodeCommandExecutionException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public ErrorCodeCommandExecutionException(String message, Throwable cause) {
    super(message, cause);
  }
  
  /**
   * Retrieves the error whom caused the exception to be raised
   * 
   * @return an error code
   */
  public int getErrorCode() {
    return Integer.valueOf(this.getMessage()).intValue();
  }

}
