/**
 * 
 */
package com.googlecode.maven.plugin.perl.util.par.exceptions;

/**
 * Exception raised if an error occured during the unpacking of a par archive
 * 
 * @author marguerp Modified by $Author: marguerp $
 * @version $Revision: #2 $
 */
public class UnableToUnpackException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public UnableToUnpackException() {
  super(); 
  }

  /**
   * @param message
   */
  public UnableToUnpackException(String message) {
    super(message);
   
  }

  /**
   * @param cause
   */
  public UnableToUnpackException(Throwable cause) {
    super(cause);
   
  }

  /**
   * @param message
   * @param cause
   */
  public UnableToUnpackException(String message, Throwable cause) {
    super(message, cause);
    
  }

}
