/**
 * 
 */
package com.googlecode.maven.plugin.perl.util;

import org.apache.maven.plugin.logging.Log;

import com.googlecode.maven.plugin.perl.util.exceptions.UnableToHandlerOutputException;


/**
 * Default class to run command line using maven api
 * 
 * @author marguerp Modified by $Author: marguerp $
 * @version $Revision: #4 $
 */
public class MavenCLIRunner extends AMavenCLIRunner {

  private Log logger = null;

  /**
   * 
   */
  public MavenCLIRunner() {
    super();
  }

  /**
     * 
     */
  public MavenCLIRunner(Log _logger) {
    super();
    this.logger = _logger;
  }

  /*
   * (non-Javadoc)
   * @see com.googlecode.maven.plugin.perl.util.AMavenCLIRunner#handlerErrorOutput(java.lang.String)
   */
  @Override
  protected void handlerErrorOutput(String line) throws UnableToHandlerOutputException {
    MavenCLIRunner.this.logError(line);

  }

  /*
   * (non-Javadoc)
   * @see
   * com.googlecode.maven.plugin.perl.util.AMavenCLIRunner#handlerStandardOutput(java.lang.String)
   */
  @Override
  protected void handlerStandardOutput(String line)  throws UnableToHandlerOutputException{

    MavenCLIRunner.this.logDebug(line);

  }

  /*
   * (non-Javadoc)
   * @see com.googlecode.maven.plugin.perl.util.AMavenCLIRunner#logDebug(java.lang.Object[])
   */
  @Override
  protected void logDebug(Object... msgs) {

    StringBuilder buf = new StringBuilder();
    for (Object obj : msgs) {
      buf.append(obj);
    }

    if (this.logger != null) {
      this.logger.debug(buf);
    }

  }

  /*
   * (non-Javadoc)
   * @see com.googlecode.maven.plugin.perl.util.AMavenCLIRunner#logError(java.lang.Exception,
   * java.lang.Object[])
   */
  @Override
  protected void logError(Exception e, Object... msgs) {
    StringBuilder buf = new StringBuilder();
    for (Object obj : msgs) {
      buf.append(obj);
    }

    if (this.logger != null) {
      this.logger.error(buf, e);
    }

  }

  /*
   * (non-Javadoc)
   * @see com.googlecode.maven.plugin.perl.util.AMavenCLIRunner#logError(java.lang.Object[])
   */
  @Override
  protected void logError(Object... msgs) {
    StringBuilder buf = new StringBuilder();
    for (Object obj : msgs) {
      buf.append(obj);
    }
    if (this.logger != null) {
      this.logger.error(buf);
    }

  }

  /*
   * (non-Javadoc)
   * @see com.googlecode.maven.plugin.perl.util.AMavenCLIRunner#logInfo(java.lang.Object[])
   */
  @Override
  protected void logInfo(Object... msgs) {
    StringBuilder buf = new StringBuilder();
    for (Object obj : msgs) {
      buf.append(obj);
    }
    if (this.logger != null) {
      this.logger.info(buf);
    }
  }

}
