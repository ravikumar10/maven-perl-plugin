/**
 * 
 */
package com.googlecode.maven.plugin.perl.test.testunit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.logging.Log;

import com.googlecode.maven.plugin.perl.test.ITestRunner;
import com.googlecode.maven.plugin.perl.util.MavenCLIRunner;
import com.googlecode.maven.plugin.perl.util.constants.CLIConstants;
import com.googlecode.maven.plugin.perl.util.constants.PerlConstants;
import com.googlecode.maven.plugin.perl.util.exceptions.CommandExecutionException;
import com.googlecode.maven.plugin.perl.util.exceptions.ErrorCodeCommandExecutionException;
import com.googlecode.maven.plugin.perl.util.exceptions.NonExistingProgramException;
import com.googlecode.maven.plugin.perl.util.exceptions.UnableToHandlerOutputException;
import com.googlecode.maven.plugin.perl.util.perl.PerlModuleFileFilter;


/**
 * Test runner for unit tests based on test::Unit library Based on TestRunner script to run test To
 * run a test : TestRunner.pl MODULE (NAME + PACKAGE)
 * 
 * @author marguerp Modified by $Author: marguerp $
 * @version $Revision: #14 $
 */
public class TestUnitTestRunner implements ITestRunner {

  private static final String DOT_PERLMODULEFILEEXT_REGEX = "\\."
      + PerlConstants.perlModuleFileExtension;

  private static final PerlModuleFileFilter FILTER = new PerlModuleFileFilter();

  private static final String TESTRUNNER_SCRIPTNAME = "TestRunner.pl";

  private Log logger = null;

  private List<String> notSuccessFulTest;

  //`TestASequenceMasker' was not a valid Test::Unit::Test
  
  private final static  int NOT_VALID_TEST_ERROR_CODE = 9;
  
//  Can't locate ASequenceMasker.pm in @INC (@INC contains: TestASequenceMasker.pm line 26.
//  BEGIN failed--compilation aborted at TestASequenceMasker.pm line 26.
  
  private final static int CAN_LOCATE_MODULE_ERROR_CODE = 2;
  
  /**
   * 
   */
  public TestUnitTestRunner() {
    super();
  }

  /**
   * 
   */
  public TestUnitTestRunner(Log _logger) {
    super();
    this.logger = _logger;
  }

  /*
   * (non-Javadoc)
   * @see com.googlecode.maven.plugin.perl.test.ITestRunner#run(java.io.File, java.util.List)
   */
  @Override
  public final void run(File testModuleDirectory, Collection<String> dependencyDirs,
      File testScriptDirectory, Map<String, String> envVars, File outputDirectory, File workingDirectory)
      throws CommandExecutionException {

    if (testModuleDirectory == null || !testModuleDirectory.exists()) {
      if (this.logger != null && this.logger.isInfoEnabled()) {
        this.logger.info("No test module to run ");
      }

    } else {

      if (outputDirectory == null) {
        throw new CommandExecutionException("No output directory provided");
      }

      if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
        throw new CommandExecutionException("Unable to create output directory");

      }

      // find modules to run

      List<String> discoveredModules = this.findModules(testModuleDirectory, null);
      if (discoveredModules == null || discoveredModules.isEmpty()) {
        if (this.logger != null && this.logger.isInfoEnabled()) {
          this.logger.info("No test module found to run.");
        }
      } else {
        if (this.logger != null && this.logger.isInfoEnabled()) {
          this.logger.info("Found " + discoveredModules.size() + " modules");
        }

        if (this.logger != null && this.logger.isDebugEnabled()) {
          this.logger.debug("Found " + discoveredModules);
        }

        // for each discovered module, runs test case

        Map<String, String> env = new HashMap<String, String>(dependencyDirs.size());
        // set environment to have access to Perl library
        env.put(PerlConstants.PERBLIB_ENV_VAR, System.getenv(PerlConstants.PERBLIB_ENV_VAR));

        if (!dependencyDirs.isEmpty()) {

          StringBuilder directoryList = new StringBuilder(testModuleDirectory.getAbsolutePath()
              + File.pathSeparator);
          for (String dir : dependencyDirs) {
            directoryList.append(dir);// .getAbsolutePath());
            directoryList.append(File.pathSeparatorChar);
          }
          directoryList.deleteCharAt(directoryList.length() - 1);
          env.put(PerlConstants.PERBLIB_ENV_VAR, directoryList.toString() + File.pathSeparatorChar
              + env.get(PerlConstants.PERBLIB_ENV_VAR));

          if (this.logger != null && this.logger.isDebugEnabled()) {
            this.logger.debug("Running command");
          }

          directoryList.setLength(0);

        }

        env.put(CLIConstants.PATH_ENV_VAR, testScriptDirectory.getAbsolutePath()
            + File.pathSeparator + System.getenv(CLIConstants.PATH_ENV_VAR));

        if (envVars != null && !envVars.isEmpty()) {
          String envPath = envVars.remove(CLIConstants.PATH_ENV_VAR);
          if (envPath != null && !envPath.isEmpty()) {
            env.put(CLIConstants.PATH_ENV_VAR, envPath
                + File.pathSeparator + env.get(CLIConstants.PATH_ENV_VAR));
          }

          String envLib = envVars.remove(PerlConstants.PERBLIB_ENV_VAR);
          if (envLib != null && !envLib.isEmpty()) {
            env.put(PerlConstants.PERBLIB_ENV_VAR, envLib
                + File.pathSeparator + env.get(PerlConstants.PERBLIB_ENV_VAR));
          }

          env.putAll(envVars);
        }

        if (this.logger != null && this.logger.isDebugEnabled()) {
          this.logger.debug("testScriptDirectory: " + testScriptDirectory.getAbsolutePath());
          this.logger.debug("Env: " + env);
        }

        if (this.logger != null && this.logger.isInfoEnabled()) {
          this.logger.info("");
          this.logger.info("-------------------------------------------------------");
          this.logger.info(" T E S T S");
          this.logger.info("-------------------------------------------------------");
        }

        int totalResultRunnedTests = 0;
        int totalResultFailedTests = 0;
        int totalResultErrorTests = 0;
        int totalResultSkippedTests = 0;

        Map<String, File> moduleLogFiles = new HashMap<String, File>();

        for (String moduleName : discoveredModules) {
          try {

            if (this.logger != null && this.logger.isInfoEnabled()) {
              this.logger.info("Running test module: " + moduleName);
            }

            // Saving output as file MODULE_NAME.txt - '::' are placed by '_'
            File outputFile = new File(outputDirectory, moduleName.replaceAll("::", "_") + ".txt");

            if (this.logger != null && this.logger.isDebugEnabled()) {
              this.logger.debug("Exporting output to " + outputFile.getAbsolutePath());
            }

            moduleLogFiles.put(moduleName, outputFile);

            TestrunnerRunner runner = new TestrunnerRunner(moduleName, outputFile, this.logger);

            List<String> command = new ArrayList<String>();
            command.add(moduleName);

            runner.run(TESTRUNNER_SCRIPTNAME, command, env, workingDirectory);
            runner.close();

            if (this.logger != null && this.logger.isInfoEnabled()) {
              this.logger.info("  Tests run: " + runner.getResultRunnedTests() + ", Failures: "
                  + runner.getResultFailedTests() + ", Errors: " + runner.getResultErrorTests()
                  + ", Skipped: " + runner.getResultSkippedTests() + ", Time elapsed: "
                  + runner.getExecutionTime() + " sec");
              totalResultRunnedTests += runner.getResultRunnedTests();
              totalResultFailedTests += runner.getResultFailedTests();
              totalResultErrorTests += +runner.getResultErrorTests();
              totalResultSkippedTests += +runner.getResultSkippedTests();

            }

          } catch (NonExistingProgramException e) {
            if (this.logger.isErrorEnabled()) {
              this.logger
                  .error(
                      "The test runner script has not been found or is not executable.",
                      e);
            }// TODO move out of the loop
            throw e;
          } catch (ErrorCodeCommandExecutionException e) {
            if(e.getErrorCode()  == NOT_VALID_TEST_ERROR_CODE )  {
              this.logger.error("The test "+moduleName+ " is not valid." , e);
            } else {
              this.logger.error("Unable to run command.", e);
            }
            throw e;
          } catch (CommandExecutionException e) {
            this.logger.error("Unable to run command.", e);
            throw e;
          } catch (IOException e) {
            throw new CommandExecutionException(e);
          }
          // TODO support for not valid test errors
        }

        if (this.notSuccessFulTest != null) {
          if (this.logger != null && this.logger.isWarnEnabled()) {
            this.logger.warn("Some tests failed.");

            for (String moduleName : this.notSuccessFulTest) {

              this.logger.info("The test module " + moduleName
                  + " failed. See log file for more detail : ");
              this.logger.info(moduleLogFiles.get(moduleName).getAbsolutePath());
            }

            this.logger.info("");
            this.logger.info("Results :");
            this.logger.info("");
            this.logger.info("Tests run: " + totalResultRunnedTests + ", Failures: "
                + totalResultFailedTests + ", Errors: " + totalResultErrorTests + ", Skipped: "
                + totalResultSkippedTests);
            this.logger.info("");
          }
          this.notSuccessFulTest.clear();
        }

      }
    }

  }

  /**
   * Finds perl module in the given directory Should it be limited to test module
   * 
   * @param testModuleDirectory directory to explorer to discover test modules
   * @return list of found modules, if any
   */
  protected final List<String> findModules(File testModuleDirectory, String perlPackage) {
    List<String> discoveredModules = null;
    File[] moduleOrDirs = testModuleDirectory.listFiles(FILTER);
    if (moduleOrDirs == null || moduleOrDirs.length == 0) {

      if (this.logger != null && this.logger.isDebugEnabled()) {
        this.logger.debug("No test module found in " + testModuleDirectory.getAbsolutePath());
      }
    } else {
      if (this.logger != null && this.logger.isDebugEnabled()) {
        this.logger.debug("found something module found in "
            + testModuleDirectory.getAbsolutePath());
      }
      for (File selectedFile : moduleOrDirs) {
        String name = selectedFile.getName();
        if (discoveredModules == null) {
          discoveredModules = new ArrayList<String>();
        }
        if (selectedFile.isFile()) {
          // found a module
          if (this.logger != null && this.logger.isDebugEnabled()) {
            this.logger.debug("found test module " + name);
          }

          // test unit file name must contain test
          String fileName = selectedFile.getName();
          if (fileName.matches(".*[tT]est.*")) {
            String moduleName = getModuleName(perlPackage, selectedFile);
            discoveredModules.add(moduleName);
          }
        } else {
          String subPerlPackage = null;
          if (perlPackage == null) {
            subPerlPackage = name;
          } else {
            subPerlPackage = this.buildPackageName(perlPackage, name);
          }
          List<String> discoveredSubModules = this.findModules(selectedFile, subPerlPackage);
          if (discoveredSubModules != null && !discoveredSubModules.isEmpty()) {
            discoveredModules.addAll(discoveredSubModules);
          }

        }

      }
    }

    return discoveredModules;
  }

  /**
   * Get module name from the module file
   * 
   * @param perlPackage currentPerlPackage
   * @param selectedFile module file
   * @return the name of the module, based on the current package and the selected file
   */
  private String getModuleName(String perlPackage, File selectedFile) {
    StringBuilder buf = new StringBuilder();
    if (perlPackage != null) {
      buf.append(perlPackage);
      buf.append(PerlConstants.PERL_PACKAGE_SEPARATOR);
    }

    if (selectedFile != null) {
      // remove file extension

      String fileName = selectedFile.getName();
      String name = fileName.replaceFirst(DOT_PERLMODULEFILEEXT_REGEX, "");
      buf.append(name);
    }

    String moduleName = buf.toString();
    buf.setLength(0);
    return moduleName;
  }

  /**
   * Build perl package
   * 
   * @param perlPackageName current perl package
   * @param subPackageName name of the sub directory
   * @return the name of the package for the sub directory
   */
  private String buildPackageName(String perlPackageName, String subPackageName) {
    StringBuilder buf = new StringBuilder();
    if (perlPackageName != null) {
      buf.append(perlPackageName);
      buf.append(PerlConstants.PERL_PACKAGE_SEPARATOR);
    }

    if (subPackageName != null) {
      buf.append(subPackageName);
    }

    String fullPackageName = buf.toString();
    buf.setLength(0);
    return fullPackageName;
  }

  private final class TestrunnerRunner extends MavenCLIRunner {

    private final Writer out;

    private final String name;

    private final String failureReportLineRegex = "Run:\\s*(\\d+),\\s*Failures:\\s*(\\d+),\\s*Errors:\\s*(\\d+).*";

    private final String successReportLineRegex = "OK\\s\\((\\d+)\\s+tests?\\)";

    private final String timeRegex = "Time:\\s+\\d+\\s+wallclock\\s+secs\\s+\\(.*=\\s+(\\d+(\\.\\d+)?)\\s+CPU\\)";

    private final Pattern failurePattern = Pattern.compile(this.failureReportLineRegex);

    private final Pattern successPattern = Pattern.compile(this.successReportLineRegex);

    private final Pattern timePattern = Pattern.compile(this.timeRegex);

    private int resultRunnedTests = 0;

    private int resultFailedTests = 0;

    private int resultErrorTests = 0;

    private int resultSkippedTests = 0;

    private double executionTime = 0;

    protected TestrunnerRunner(String _name, File outputFile, Log _logger) throws IOException {
      super(_logger);

      FileWriter fstream = new FileWriter(outputFile);
      this.out = new BufferedWriter(fstream);
      this.name = _name;
    }

    // }catch (Exception e
    // ){//Catch exception if any
    // System.err.println("Error: " + e.getMessage());
    // }
    // }

    /*
     * (non-Javadoc)
     * @see com.googlecode.maven.plugin.perl.util.AMavenCLIRunner#handlerErrorOutput(java.lang
     * .String)
     */
    @Override
    protected void handlerErrorOutput(String line) throws UnableToHandlerOutputException {
      try {
        this.out.write(line);
        this.out.write("\n");
        this.out.flush();
      } catch (IOException e) {
        throw new UnableToHandlerOutputException(e);
      }

      this.extractReport(line);
      this.testIfFailure(line);
    }

    /**
     * Extract data from report lines Time / number of runned tests etc
     * 
     * @param line text line to extract data from, if any
     */
    private void extractReport(String line) {
      // FAILURE Run: 5, Failures: 0, Errors: 3
      // SUCCESS OK (2 tests)
      // TRY FAILURE MODE
      Matcher matcher = this.failurePattern.matcher(line);
      boolean matchFound = matcher.find();
      if (matchFound) {
        // the result line
        this.resultRunnedTests = Integer.parseInt(matcher.group(1));
        this.resultFailedTests = Integer.parseInt(matcher.group(2));
        this.resultErrorTests = Integer.parseInt(matcher.group(3));

      } else {
        // OK

        Matcher successmatcher = this.successPattern.matcher(line);
        boolean successmatchFound = successmatcher.find();
        if (successmatchFound) {
          // the result line
          this.resultRunnedTests = Integer.parseInt(successmatcher.group(1));
          this.resultFailedTests = 0;
          this.resultErrorTests = 0;

        } else {
          // extract execution duration
          // Time: 1 wallclock secs ( 0.08 usr 0.03 sys + 0.58 cusr 0.07 csys = 0.76 CPU)
          Matcher timeMatcher = this.timePattern.matcher(line);
          boolean timeMatchFound = timeMatcher.find();
          if (timeMatchFound) {
            this.executionTime = Double.parseDouble(timeMatcher.group(1));
          }
        }
      }
    }

    /*
     * (non-Javadoc)
     * @see com.googlecode.maven.plugin.perl.util.AMavenCLIRunner#handlerStandardOutput(java
     * .lang.String)
     */
    @Override
    protected void handlerStandardOutput(String line) throws UnableToHandlerOutputException {
      try {
        this.out.write(line);
        this.out.write("\n");
        this.out.flush();
      } catch (IOException e) {
        throw new UnableToHandlerOutputException(e);
      }
      this.extractReport(line);
      this.testIfFailure(line);
    }

    /**
     * @param line
     */
    private void testIfFailure(String line) {
      // Failure when
      // !!!FAILURES!!!
      // Test Results:
      // Run: 4, Failures: 0, Errors: 1
      //
      // There was 1 error:

      if (line.contains("Test was not successful.")) {
        // The test failed

        if (TestUnitTestRunner.this.notSuccessFulTest == null) {
          TestUnitTestRunner.this.notSuccessFulTest = new ArrayList<String>();
        }
        TestUnitTestRunner.this.notSuccessFulTest.add(this.name);
      }
    }

    /**
     * Closes the handling
     * 
     * @throws IOException
     */
    public void close() throws IOException {
      // Close the output stream

      if (this.out != null) {
        this.out.close();
      }
    }

    /**
     * @return the resultRunnedTests
     */
    protected int getResultRunnedTests() {
      return this.resultRunnedTests;
    }

    /**
     * @return the resultFailedTests
     */
    protected int getResultFailedTests() {
      return this.resultFailedTests;
    }

    /**
     * @return the resultErrorTests
     */
    protected int getResultErrorTests() {
      return this.resultErrorTests;
    }

    /**
     * @return the resultSkippedTests
     */
    protected int getResultSkippedTests() {
      return this.resultSkippedTests;
    }

    /**
     * @return the executionTime
     */
    protected double getExecutionTime() {
      return this.executionTime;
    }
  }
}
