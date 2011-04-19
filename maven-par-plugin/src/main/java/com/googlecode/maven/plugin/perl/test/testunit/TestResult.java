/**
 * 
 */
package com.googlecode.maven.plugin.perl.test.testunit;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO add description
 * 
 * @author marguerp Modified by $Author: marguerp $
 * @version $Revision: #1 $
 */
public class TestResult {

  
  
  
  /**
   * Field _time.
   */
  private double time;


  /**
   * Field _name.
   */
  private String name;
  
  
  
  /**
   * Field _failures.
   */
  private int failures =  0;
  
  /**
   * Field _skipped.
   */
  private int skipped = 0;
  
  /**
   * Field _tests.
   */
  private int tests = 0; 
  
  /**
   * Field _errors.
   */
  private int errors = 0;
  
  private List<Test> testList = null;
  
  /**
   * 
   */
  public TestResult() {
    super ();
    this.testList = new ArrayList<Test>();
  }

  
  
  /**
   * @param time
   * @param name
   */
  public TestResult(double _time, String _name) {
    super();
    this.time = _time;
    this.name = _name;
    
   this.testList = new ArrayList<Test>();
  }





  public void addFailedTest( String testName, double testTime) {
    this.failures ++;
    this.tests ++;
    
    Test aTest = new Test(testName, testTime);
    this.testList.add(aTest) ;
  }
  
  public void addOKTest( String testName, double testTime) {
    this.tests ++;
    
    Test aTest = new Test(testName, testTime);
    this.testList.add(aTest) ;
  }
  
  
  

  
   /**
   * @return the time
   */
  public double getTime() {
    return this.time;
  }



  /**
   * @return the name
   */
  public String getName() {
    return this.name;
  }



  /**
   * @return the failures
   */
  public int getFailures() {
    return this.failures;
  }



  /**
   * @return the skipped
   */
  public int getSkipped() {
    return this.skipped;
  }



  /**
   * @return the tests
   */
  public int getTests() {
    return this.tests;
  }



  /**
   * @return the errors
   */
  public int getErrors() {
    return this.errors;
  }



  /**
   * @return the testList
   */
  public List<Test> getTestList() {
    return this.testList;
  }





  protected class Test {
     
     private String testName = null;
     
     private double testTime = 0.0f;

    /**
     * @param name
     * @param time
     */
    public Test(String _name, double _time) {
      super();
      this.testName = _name;
      this.testTime = _time;
    }

    /**
     * @return the name
     */
    public String getName() {
      return this.testName;
    }

    /**
     * @param name the name to set
     */
    public void setName(String _name) {
      this.testName = _name;
    }

    /**
     * @return the time
     */
    public double getTime() {
      return this.testTime;
    }

    /**
     * @param time the time to set
     */
    public void setTime(double _time) {
      this.testTime = _time;
    }
     
     
   }
}
