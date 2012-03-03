/**
 * 
 */
package com.googlecode.maven.plugin.perl.test.testunit;

import java.io.File;

import org.junit.Test;

import com.googlecode.maven.plugin.perl.test.testunit.TestUnitTestRunner;
import com.googlecode.maven.plugin.perl.util.exceptions.CommandExecutionException;


/**
 * TODO add description
 * 
 * @author marguerp Modified by $Author: marguerp $
 * @version $Revision: #2 $
 */
public class TestUnitTestRunnerTest {

  /**
   * Test method for {@link com.googlecode.maven.plugin.perl.test.testunit.TestUnitTestRunner#run(java.io.File, java.util.Collection, java.io.File)}.
   * @throws CommandExecutionException 
   */
  @Test
  public void testRun() throws CommandExecutionException {
    TestUnitTestRunner runner = new TestUnitTestRunner();
    runner.run(new File ("src/test/resources"), null, new File ("src/test/resources"), null, new File ("target/surefire/"),new File ("."));
  }

}
