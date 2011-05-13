/**
 * 
 */
package com.googlecode.maven.plugin.perl.util;

import org.junit.Test;

import com.googlecode.maven.plugin.perl.util.exceptions.CommandExecutionException;


/**
 * TODO add description
 * 
 * @author marguerp Modified by $Author: marguerp $
 * @version $Revision: #1 $
 */
public class MavenCLIRunnerTest {

  /**
   * Test method for {@link com.googlecode.maven.plugin.perl.util.MavenCLIRunner#run(java.lang.String, java.util.List, java.util.Map, java.io.File)}.
   * @throws CommandExecutionException 
   */
  @Test
  public void testRun() throws CommandExecutionException {
   // fail("Not yet implemented");
    MavenCLIRunner runner = new MavenCLIRunner();
    
    runner.run("ls", null, null, null);
  }

}
