package com.googlecode.maven.plugin.perl.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.DefaultMavenProjectBuilder;
import org.apache.maven.project.MavenProject;

import com.googlecode.maven.plugin.perl.test.testunit.TestUnitTestRunner;
import com.googlecode.maven.plugin.perl.util.constants.CLIConstants;
import com.googlecode.maven.plugin.perl.util.exceptions.CommandExecutionException;
import com.googlecode.maven.plugin.perl.util.maven.MavenPerlDependencyManager;


/**
 * Goal that unit test a perl application. Test::Unit library perl library is required Runs test
 * perl modules
 * 
 * @goal test
 * @phase test
 * @requiresDependencyResolution test
 */
public class PerlTestMojo extends AbstractMojo {

  private static final String LIB_TESTUNIT_SUBDIR = "Test";

  /**
   * The maven project.
   * 
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;

  /**
   * Build directory.
   * 
   * @parameter expression="${project.build.directory}"
   * @required
   */
  private File buildDirectory;

  /**
   * The maven archive configuration to use
   * 
   * @parameter
   * @readonly
   */
  //private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

  /**
   * directory containing perl main scripts
   * &lt;scriptSourceDirectory&gt;${basedir}/src/main/scripts&lt;/scriptSourceDirectory&gt;
   * 
   * @parameter expression="${project.build.scriptSourceDirectory}"
   *            default-value="${project.basedir}/src/main/scripts/"
   * @required
   */
  private File scriptDirectory;

  
  /**
   * project base directory
   * 
   * @parameter expression="${project.basedir}"
   * @required
   * @readonly
   * 
   */
  private File baseDirectory;

  /**
   * Directory containing test perl main scripts
   * 
   * @parameter expression="${project.basedir}/src/test/scripts/"
   * @required
   */
  private File testScriptDirectory;

  /**
   * module directory containing project perl source modules
   * 
   * @parameter expression="${par.moduledir}" default-value="${project.build.sourceDirectory}"
   * @required
   */
  private File moduleDirectory;

  /**
   * test module directory containing project perl source modules
   * 
   * @parameter expression="${par.testmoduledir}"
   *            default-value="${project.build.testSourceDirectory}"
   * @required
   */
  private File testModuleDirectory;

  /**
   * module build directory to containing project perl modules for build
   * 
   * @parameter expression="${project.build.outputDirectory}"
   * @required
   * @readonly
   */
  private File moduleBuildDirectory;

  /**
   * module build directory to containing project perl modules for build
   * 
   * @parameter expression="${project.build.outputDirectory}/../lib/"
   * @required
   * @readonly
   */
  private File libBuildDirectory;

  /**
   * test module build directory to containing project perl modules for build
   * 
   * @parameter expression="${project.build.testOutputDirectory}"
   * @required
   * @readonly
   */
  private File testModuleBuildDirectory;

  /**
   * module build directory to containing project perl modules for build
   * 
   * @parameter expression="${project.build.testOutputDirectory}/../test-lib/"
   * @required
   * @readonly
   */
  private File testLibBuildDirectory;

  /**
   * List of additional libraries to be added to the classpath for test running
   * 
   * @parameter expression="${par.libraries}"
   * @optional
   */
  // private String[] additionalLibraries; not needed for test

  /**
   * Map of environment variables to be set for running tests
   * 
   * @parameter
   * @optional
   */
  private Map<String, String> testEnvVars;

  /**
   * @parameter expression="${maven.test.skip}"
   * @optional
   * @readonly
   */
  private boolean skipTest;

  /** @component */
  private org.apache.maven.artifact.factory.ArtifactFactory artifactFactory;

  /** @component */
  private org.apache.maven.artifact.resolver.ArtifactResolver resolver;

  /**
   * Used to build a maven projects from artifacts in the remote repository.
   * 
   * @component role="org.apache.maven.project.MavenProjectBuilder"
   * @required
   * @readonly
   */
  private DefaultMavenProjectBuilder projectBuilder;

  /**
   * Location of the local repository.
   * 
   * @parameter expression="${localRepository}"
   * @readonly
   * @required
   */
  private org.apache.maven.artifact.repository.ArtifactRepository localRepository;

  /**
   * List of Remote Repositories used by the resolver
   * 
   * @parameter expression="${project.remoteArtifactRepositories}"
   * @readonly
   * @required
   */
  private ArrayList<?> remoteRepositories;

  // ArrayList mandatory/ otherwise maven 2.2.1 fails

  /**
   * @component
   */
  // private MavenProjectHelper mavenProjectHelper;

  /**
   * Directory for test report
   * 
   * @parameter expression="${basedir}/target/surefire-reports"
   * @readonly
   * @required
   */
  private File testReportOutputDirectory;

  /** {@inheritDoc} */
  public final void execute() throws MojoExecutionException {
    Log logger = this.getLog();

    if (logger.isInfoEnabled()) {
      logger.info("Executing perl testing plugin");
    }
    if (logger.isDebugEnabled()) {
      logger.debug(this.buildDirectory.getAbsolutePath());

      logger.debug("testModuleDirectory " + this.testModuleDirectory);
      logger.debug("testEnvVars " + this.testEnvVars);
    }

    if (this.skipTest) {

      if (logger.isInfoEnabled()) {
        logger.info("Skipping test");
      }
    } else {
      // Extract library archives
      // Run test modules and generate XML output
      //
      try {

        // in any case, copy modules to build directory
        if (this.moduleDirectory == null || !this.moduleDirectory.exists()) {
          logger.warn("No source directory");
        }

        if (!this.moduleBuildDirectory.exists() && !this.moduleBuildDirectory.mkdirs()) {
            throw new MojoExecutionException("Unable to create  build directory. "
                + this.moduleBuildDirectory.getAbsoluteFile());
          }
        

        if (!this.testModuleBuildDirectory.exists() && !this.testModuleBuildDirectory.mkdirs()) {
            throw new MojoExecutionException("Unable to create test build directory. "
                + this.testModuleBuildDirectory.getAbsoluteFile());
          
        }

        MavenPerlDependencyManager depManager = new MavenPerlDependencyManager(logger,
            this.projectBuilder, this.localRepository, this.remoteRepositories,
            this.artifactFactory, this.resolver);

        Collection<String>  libPaths = new ArrayList<String>();
        libPaths.add(this.moduleBuildDirectory.getAbsolutePath());
        libPaths.add(this.testModuleBuildDirectory.getAbsolutePath());

        libPaths.add(this.libBuildDirectory.getAbsolutePath());
        String testLibBuildDirAbsolutePath = this.testLibBuildDirectory.getAbsolutePath();
        libPaths.add(testLibBuildDirAbsolutePath);
        
        libPaths.addAll( depManager.extractProjectDependencies(this.project,
            this.libBuildDirectory, true, this.testLibBuildDirectory));

        depManager.copyProjectModules(this.moduleDirectory, this.moduleBuildDirectory);
        depManager.copyProjectModules(this.testModuleDirectory, this.testModuleBuildDirectory);
  

        ITestRunner runner = new TestUnitTestRunner(this.getLog());

        if (this.testEnvVars == null) {
          this.testEnvVars = new HashMap<String, String>();
        }

        String env_path = this.testEnvVars
        .get(CLIConstants.PATH_ENV_VAR);
       
        if( env_path == null ) {
          env_path =  this.scriptDirectory.getAbsolutePath()
          .concat(File.pathSeparator).concat( testLibBuildDirAbsolutePath+File.separator+LIB_TESTUNIT_SUBDIR)// work aroud for having TestRunner.pl script directory in the path
              .concat(File.pathSeparator).concat( this.libBuildDirectory.getAbsolutePath())
              .concat(File.pathSeparator).concat( testLibBuildDirAbsolutePath) 
              ;
        
        } else {
          env_path = 
            env_path.concat(File.pathSeparator).concat(this.scriptDirectory.getAbsolutePath())
             .concat(File.pathSeparator).concat( testLibBuildDirAbsolutePath+File.separator+LIB_TESTUNIT_SUBDIR)    
            .concat(File.pathSeparator).concat( this.libBuildDirectory.getAbsolutePath())
            .concat(File.pathSeparator).concat(testLibBuildDirAbsolutePath)
           
        ;
        }

        
        if (this.scriptDirectory != null && this.scriptDirectory.exists()) {

          env_path = env_path.concat(File.pathSeparator).concat(this.scriptDirectory.getAbsolutePath());
        }
        
        this.testEnvVars.put(CLIConstants.PATH_ENV_VAR,
            env_path);
               
       


        runner.run(this.testModuleBuildDirectory, libPaths, this.testScriptDirectory,
            this.testEnvVars, this.testReportOutputDirectory,this.baseDirectory);

      } catch (CommandExecutionException e) {
        throw new MojoExecutionException("Unable to run test.", e);
      }
    }

  }
}
