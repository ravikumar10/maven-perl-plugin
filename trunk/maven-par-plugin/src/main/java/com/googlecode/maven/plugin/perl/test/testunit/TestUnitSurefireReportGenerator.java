/**
 * 
 */
package com.googlecode.maven.plugin.perl.test.testunit;

import java.io.File;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.plugin.logging.Log;

import com.googlecode.maven.plugin.perl.test.testunit.TestResult.Test;
import com.googlecode.maven.plugin.perl.test.testunit.xml.exceptions.UnableToGenerateSureFireReport;
import com.googlecode.maven.plugin.perl.test.testunit.xml.mapping.Property;
import com.googlecode.maven.plugin.perl.test.testunit.xml.mapping.Testcase;
import com.googlecode.maven.plugin.perl.test.testunit.xml.mapping.Testsuite;


/**
 * Claass for generating test execution report in the sufire format
 * 
 * @author marguerp Modified by $Author: marguerp $
 * @version $Revision: #1 $
 */
public class TestUnitSurefireReportGenerator {

  private Log logger = null;

  /**
   * 
   */
  public TestUnitSurefireReportGenerator() {
    super();
  }

  /**
   * 
   */
  public TestUnitSurefireReportGenerator(Log _logger) {
    super();
    this.logger = _logger;
  }

  public void generateReport(TestResult result, Map<String, String> env, File outputDirectory)
      throws UnableToGenerateSureFireReport {
    // <testsuite failures="0" time="1.119" errors="0" skipped="0" tests="1"
    // name="TestDeployment">
    // <testcase time="0.961" classname="TestDeployment"
    // name="test"/>

    if (result == null) {

      if (this.logger != null && this.logger.isErrorEnabled()) {
        this.logger.error("No module name provided");
      }
      throw new UnableToGenerateSureFireReport();
    }

    Testsuite testsuite = new Testsuite();
   
    testsuite.setTime(new BigDecimal(result.getTime()));
    
   

    testsuite.setName(result.getName());

    Properties systemProperties = System.getProperties();

    if (env != null && !env.isEmpty()) {
      systemProperties.putAll(env);
    }

    if (!systemProperties.isEmpty()) {
      com.googlecode.maven.plugin.perl.test.testunit.xml.mapping.Properties props = new com.googlecode.maven.plugin.perl.test.testunit.xml.mapping.Properties();

      for (Map.Entry<Object, Object> prop : systemProperties.entrySet()) {

        Property proppp = new Property();
        proppp.setName((String) prop.getKey());
        proppp.setValue((String) prop.getValue());

        props.addProperty(proppp);
      }
      testsuite.setProperties(props);
    }
    
    //foreach method
    
    int testCount =  result.getTests();
    int errorCount = result.getErrors();
    int failureCount = result.getFailures();
    int skippedCount = 0;
    
    for (Test aTest : result.getTestList()) {
   
    Testcase testcase = new Testcase();
    testcase.setClassname(result.getName());
    testcase.setName(aTest.getName());
    testcase.setTime(new BigDecimal(aTest.getTime()));
    testsuite.addTestcase(testcase);
    }
    
    testsuite.setFailures(failureCount);
    testsuite.setErrors(errorCount);
    testsuite.setSkipped(skippedCount);
    testsuite.setTests(testCount);
    }
}

// Map<methodName (time, )
