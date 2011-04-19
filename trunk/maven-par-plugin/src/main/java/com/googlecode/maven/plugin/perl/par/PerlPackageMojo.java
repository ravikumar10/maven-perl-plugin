package com.googlecode.maven.plugin.perl.par;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.DefaultMavenProjectBuilder;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.jar.ManifestException;

import com.googlecode.maven.plugin.perl.par.util.PARPacker;
import com.googlecode.maven.plugin.perl.util.constants.PerlConstants;
import com.googlecode.maven.plugin.perl.util.maven.MavenPerlDependencyManager;


/**
 * Goal that packages a perl application. PAR::Packer library perl library is required Saves modules
 * as a par archive (main artifact) Package script (create an executable archive with all required
 * libraries) Decompress dependencies (saved as PAR), adds lib directory to PERL5LIB environment
 * variable launch pp command additional libraries can be specified to be added to the final file.
 * 
 * @goal par
 * @phase package
 */
public class PerlPackageMojo extends AbstractMojo {
  private static final String ERROR_COPYING_LIBS_MSG = "Error copying libs";

  /**
   * Location of the file.
   * 
   * @parameter expression="${project.build.directory}"
   * @required
   * @readonly
   */
  private File outputDirectory;

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
   * The Jar archiver needed for archiving.
   * 
   * @component role="org.codehaus.plexus.archiver.Archiver" roleHint="jar"
   * @required
   * @readonly
   */
  private JarArchiver jarArchiver;

  /**
   * The maven archive configuration to use
   * 
   * @parameter
   * @readonly
   */
  protected MavenArchiveConfiguration archive = new MavenArchiveConfiguration();
  
  /**
   * module build directory to containing project perl modules for build
   * 
   * @parameter expression="${project.build.outputDirectory}/../lib/"
   * @required
   * @readonly
   */
  private File libBuildDirectory;

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
   * module directory containing project perl source modules
   * 
   * @parameter expression="${par.moduledir}" default-value="${project.build.sourceDirectory}"
   * @required
   */
  private File moduleDirectory;

  /**
   * module build directory to containing project perl modules for build
   * 
   * @parameter expression="${project.build.outputDirectory}"
   * @required
   * @readonly
   */
  private File moduleBuildDirectory;

  /**
   * List of additional libraries to be added to the par for script execution
   * 
   * @parameter expression="${par.libraries}"
   * @optional
   */
  private String[] additionalLibraries;
  
  /**
   * List of additional data file to be added to the par for script execution
   * 
   * @parameter expression="${par.data}"
   * @optional
   */
  private String[] additionalDataFiles;

  /**
   * List of additional external module to be added to the par for script execution
   * 
   * @parameter expression="${par.mainscript}"
   * @optional
   */
  private  String[]  scripts;
  //<String>

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

  /**
   * @component
   */
  public MavenProjectHelper mavenProjectHelper;
  

  /** {@inheritDoc}
   *  running packaging of the current project
   *  may package a script for execution
   *  Dependencies libraries are copied in the 
   * 
   * 
    */
  public void execute() throws MojoExecutionException {
    Log logger = this.getLog();

    if (logger.isInfoEnabled()) {
      logger.info("Executing perl packaging plugin");
    }
    if (logger.isDebugEnabled()) {
      logger.debug(this.outputDirectory.getAbsolutePath() + this.buildDirectory.getAbsolutePath());

      if (this.scripts != null && this.scripts.length>0) {
        logger.debug("Found scripts to package");
        for (String scriptName : this.scripts) {
          logger.debug(scriptName);
        }
      }
      logger.debug("scriptDirectory " + this.scriptDirectory);
    }

    // Compute archive name
    try {

      PARPacker packer = new PARPacker(logger);

      // in any case, copy modules to build directory
      if (this.moduleDirectory == null || !this.moduleDirectory.exists()) {
        logger.warn("No source directory");
      }
      
      if (!this.moduleBuildDirectory.exists() && !this.moduleBuildDirectory.mkdirs()) {
          throw new MojoExecutionException("Unable to create build directory. "
              + this.moduleBuildDirectory.getAbsoluteFile());
        }

      
      MavenPerlDependencyManager depManager = new MavenPerlDependencyManager(logger,
          this.projectBuilder, this.localRepository, this.remoteRepositories,
          this.artifactFactory, this.resolver);

      Collection<String> libPaths = new ArrayList<String>();
      
      libPaths.add(this.moduleBuildDirectory.getAbsolutePath());

      libPaths.add(this.libBuildDirectory.getAbsolutePath());
      
    libPaths.addAll( depManager.extractProjectDependencies(this.project,
          this.libBuildDirectory, false, null));

      depManager.copyProjectModules(this.moduleDirectory, this.moduleBuildDirectory);

      if (libPaths == null) {
        libPaths = new ArrayList<String>();
      }


      this.packageScripts(packer, libPaths);

      File libFile = packageLibrary(packer);
      this.project.getArtifact().setFile(libFile);

    } catch (IOException e) {
      throw new MojoExecutionException("Unable to pack script.", e);
    } catch (ArchiverException e) {
      throw new MojoExecutionException("Error creating library package", e);
    } catch (ManifestException e) {
      throw new MojoExecutionException("Error creating library package", e);
    } catch (DependencyResolutionRequiredException e) {
      throw new MojoExecutionException("Error creating library package", e);
    }

  }

  /**
   * Package the library : 
   * creates a PAR archive of the perl modules
   * 
   * @param packer PARPacker object
   * @return the file object of the created archive
   * 
   * @throws ArchiverException
   * @throws ManifestException
   * @throws IOException
   * @throws DependencyResolutionRequiredException
   */
  private File packageLibrary(PARPacker packer) throws ArchiverException, ManifestException,
      IOException, DependencyResolutionRequiredException {
    // create a simple library archive
    // main artifact

    String libArchiveName = this.project.getBuild().getFinalName() + ".par";// -lib.zip";
    File libFile = new File(this.buildDirectory, libArchiveName);

    MavenArchiver archiver = new MavenArchiver();
    archiver.setArchiver(this.jarArchiver);
    archiver.setOutputFile(libFile);
    packer.createLibraryPackage(archiver, this.moduleBuildDirectory, this.project, this.archive);
    return libFile;
  }

  /**
   * Package scripts  : 
   * creates a PAR /executable archive for each specified scripts
   * 
   * @param packer PARPacker object
   * @return the file object of the created archive
   * 
   * @param logger
   * @param packer
   * @throws IOException
   * @throws MojoExecutionException
   */
  private void packageScripts( PARPacker packer,  Collection<String> libPaths ) throws IOException,
      MojoExecutionException {
    
    Log logger = this.getLog();
    
    if (this.scripts != null && this.scripts.length>0) {
      // create a full package application

    
    
      // packages are decompressed,
      // perb libs are in the lib sub-directory.
  

      for (String scriptName :  this.scripts) {
        // if a script name is provided, it will be packaged using PAR::Packer

        File scriptFile = new File(scriptName);
        String archiveName = scriptFile.getName();
        File parFile = new File(this.buildDirectory, archiveName);

        // set archive to the main artifact
        this.mavenProjectHelper.attachArtifact(this.project, PerlConstants.PAR_MAVEN_PROJECT_TYPE, scriptName,
            parFile);

        packer.pp(scriptName, parFile, this.scriptDirectory, Arrays
            .asList(this.additionalLibraries), Arrays
            .asList(this.additionalDataFiles), libPaths);
      }
    } else {
      if (logger.isInfoEnabled()) {
        logger.info("No script to package");
      }
    }
  }

  

 
}
