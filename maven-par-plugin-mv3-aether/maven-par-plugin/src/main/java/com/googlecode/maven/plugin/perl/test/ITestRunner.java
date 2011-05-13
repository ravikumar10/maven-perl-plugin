/**
 * 
 */
package com.googlecode.maven.plugin.perl.test;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import com.googlecode.maven.plugin.perl.util.exceptions.CommandExecutionException;


/**
 *Interface for testing perl test module
 * 
 * @author marguerp Modified by $Author: marguerp $
 * @version $Revision: #5 $
 */
public interface ITestRunner {

  void run(File testModuleDirectory, Collection<String> dependencyDirs, File testScriptDirectory, Map<String, String> envVars, File outputDirectory, File workingDirectory) throws CommandExecutionException;
  
}
