/**
 * 
 */
package com.googlecode.maven.plugin.perl.par.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.ManifestException;

import com.googlecode.maven.plugin.perl.util.MavenCLIRunner;
import com.googlecode.maven.plugin.perl.util.constants.CLIConstants;
import com.googlecode.maven.plugin.perl.util.constants.PerlConstants;
import com.googlecode.maven.plugin.perl.util.exceptions.CommandExecutionException;
import com.googlecode.maven.plugin.perl.util.exceptions.ErrorCodeCommandExecutionException;
import com.googlecode.maven.plugin.perl.util.exceptions.NonExistingProgramException;


/**
 * @author pierre
 */
public class PARPacker {

  private static final String PP_COMMAND = "pp";// "/usr/bin/pp";

  private static final String OUTPUT_PARAMETER = "-o";

  private static final String LIB_PARAMETER = "-M";
  
  private static final String DATA_PARAMETER = "-a";

  public static final String LIB_INTERNAL_DIR_PATH = "lib";

  private Log logger = null;

  /**
	 * 
	 */
  public PARPacker(Log _logger) {
    super();
    this.logger = _logger;
  }

  /**
   * Executes the pp tool to package a script
   * 
   * @param script file name of the script to package
   * @param outputFilePath the name of the file to create
   * @param workingDirectory working directory, containing the script file
   * @param additionalLibs list of additional module names to add the package for the execution of
   *          the script
   * @param libDirectories list of paths containing libraries required by the scripts
   * @throws IOException
   */
  public void pp(String script, File outputFilePath, File workingDirectory,
      List<String> additionalLibs,  List<String> additionalDataFiles, Collection<String> libDirectories) throws IOException {

    if (script != null && !script.isEmpty()) {

      if (this.logger.isInfoEnabled()) {
        this.logger.info("Generating packaging");
      }

      List<String> command = new ArrayList<String>();
      // command.add(PP_COMMAND);

      if (additionalLibs != null && !additionalLibs.isEmpty()) {
        for (String module : additionalLibs) {
          command.add(LIB_PARAMETER);
          command.add(module);

        }
      }

      if (additionalDataFiles != null && !additionalDataFiles.isEmpty()) {
        for (String file : additionalDataFiles) {
          command.add(DATA_PARAMETER);
          command.add("'"+file+";/'");
        }
      }
      
      if (outputFilePath == null) {
        throw new IOException("No output file provided. Can not create par file.");
      }

      if ((outputFilePath.exists() && outputFilePath.canWrite())
          || outputFilePath.getParentFile().canWrite()) {

        command.add(OUTPUT_PARAMETER);

        command.add(outputFilePath.getAbsolutePath());

      } else {
        throw new IOException("Unable to write output file " + outputFilePath.getAbsolutePath()
            + "  is not writable.");
      }

      String scriptPath = script;
      if (workingDirectory != null) {
        scriptPath = workingDirectory.getAbsolutePath() + File.separator + scriptPath;
      }
      command.add(scriptPath);

      Map<String, String> env = new HashMap<String, String>(libDirectories.size());
      // set environment to have access to Perl library
      env.put(PerlConstants.PERBLIB_ENV_VAR, System.getenv(PerlConstants.PERBLIB_ENV_VAR));

      if (!libDirectories.isEmpty()) {

        StringBuilder directoryList = new StringBuilder();
        for (String dir : libDirectories) {
          directoryList.append(dir);
          directoryList.append(File.pathSeparatorChar);
        }
        directoryList.deleteCharAt(directoryList.length() - 1);
        String perl5lib_value = env.get(PerlConstants.PERBLIB_ENV_VAR);
        
        StringBuffer varValue = new StringBuffer(directoryList);
        
        // TODO not add the environment  PERL5LIB var content if not set 
        if( perl5lib_value != null  && ! perl5lib_value.isEmpty()) {
          varValue.append(File.pathSeparatorChar);
          
          varValue.append(perl5lib_value);
        }
        
        env.put(PerlConstants.PERBLIB_ENV_VAR, varValue.toString());
        varValue.setLength(0);

        if (this.logger.isDebugEnabled()) {
          this.logger.debug("Running PAR::Packer with lib directories " + directoryList);
        }

        directoryList.setLength(0);

      }

      StringBuilder envPath= new StringBuilder(System.getenv(CLIConstants.PATH_ENV_VAR));
      // add library directories, as they may contain scripts

     
      for (String dir : libDirectories) {
        envPath.append(dir);
        envPath.append(File.pathSeparatorChar);
      }
       
      
      env.put(CLIConstants.PATH_ENV_VAR, envPath.toString());
      if (this.logger.isDebugEnabled()) {
        this.logger.debug("Env : " + env);
      }

      if (this.logger.isInfoEnabled()) {
        this.logger.info("Running PAR::Packer : " + outputFilePath.getAbsolutePath());
      }

      try {
        MavenCLIRunner runner = new MavenCLIRunner(this.logger);
        runner.run(PP_COMMAND, command, env, workingDirectory);
      } catch (NonExistingProgramException e) {
        this.logger
            .error(
                "The PAR::Packer is not installed. Your script can be packaged. Please install the Par::Packer library.",
                e);
        throw new IOException(e);
      } catch (ErrorCodeCommandExecutionException e) {
        if (e.getErrorCode() == 2) {
          if (this.logger.isErrorEnabled()) {
            this.logger
                .error("The PAR::Packer is not installed. Your script can not be packaged. Please install the Par::Packer library.");
          }
          if (this.logger.isDebugEnabled()) {
            this.logger.debug("", e);
          }
        } else {
          this.logger.error("Unable to run command.", e);
        }
        throw new IOException(e);
      } catch (CommandExecutionException e) {
        this.logger.error("Unable to run command.", e);
        throw new IOException(e);
      }

    } else {
      if (this.logger.isInfoEnabled()) {
        this.logger.info("No script specified. No packaging.");
      }
    }

  }

  /**
   * Creates a packaging for a whole library, not script
   * 
   * @param archiver
   * @param libDirectory
   * @param project
   * @param archive
   * @throws ArchiverException
   * @throws ManifestException
   * @throws IOException
   * @throws DependencyResolutionRequiredException
   */
  public void createLibraryPackage(MavenArchiver archiver, File libDirectory, MavenProject project,
      MavenArchiveConfiguration archive) throws ArchiverException, ManifestException, IOException,
      DependencyResolutionRequiredException {

    // archive libs
    if (libDirectory != null && libDirectory.exists()) {
      archiver.getArchiver().addDirectory(libDirectory, "lib/");
      // create archive
      archiver.createArchive(project, archive);
    } else {
      this.logger.warn("No library to package. Nothing to do");
    }

  }

}
