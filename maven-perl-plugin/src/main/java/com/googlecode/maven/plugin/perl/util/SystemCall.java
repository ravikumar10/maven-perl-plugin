package com.googlecode.maven.plugin.perl.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;

import com.googlecode.maven.plugin.perl.util.exceptions.CommandExecutionException;
import com.googlecode.maven.plugin.perl.util.exceptions.NonExistingProgramException;


/**
 * Class to execute external commands
 * 
 * @author marguerp Modified by $Author: marguerp $
 * @version $Revision: #3 $
 */
public class SystemCall implements ICommandRunner {

  private Log logger = null;

  /**
   * Creates a new instance of SystemCallExample
   * 
   * @param _logger : org.apache.maven.plugin.logging.Log object
   */
  public SystemCall(Log _logger) {

    super();
    this.logger = _logger;
  }

  public void run(String exec, List<String> commandParameters, Map<String, String> env,
      File workingDirectory) throws CommandExecutionException {
    if (commandParameters != null && !commandParameters.isEmpty()) {

      if (this.logger.isInfoEnabled()) {
        this.logger.info("Running command " + commandParameters.toString());
      }

      ProcessBuilder processBuilder = new ProcessBuilder(commandParameters);

      if (env != null && !env.isEmpty()) {
        Map<String, String> processEnv = processBuilder.environment();
        processEnv.putAll(env);
      }

      processBuilder.directory(workingDirectory);
      Process p;

      InputStream is = null;
      InputStreamReader isr = null;
      BufferedReader br = null;

      InputStream es = null;
      InputStreamReader esr = null;
      BufferedReader ber = null;
      try {
        p = processBuilder.start();

        is = p.getInputStream();
        isr = new InputStreamReader(is);
        br = new BufferedReader(isr);

        es = p.getErrorStream();
        esr = new InputStreamReader(es);
        ber = new BufferedReader(esr);

        String line;

        // Read the output
        while ((line = br.readLine()) != null) {
          if (this.logger.isInfoEnabled()) {
            this.logger.info(line);
          }
        }

        // Read the error output
        while ((line = ber.readLine()) != null) {
          if (this.logger.isErrorEnabled()) {
            this.logger.error(line);
          }
        }

        // Check for failure
        if (p.exitValue() != 0) {
          if (this.logger.isErrorEnabled()) {
            this.logger.error("exit value= " + p.exitValue());
          }
        }

      } catch (IOException io) {
        if (this.logger.isErrorEnabled()) {
          this.logger.error("", io);
        }
        String message = io.getMessage();

        if (message != null && message.startsWith("Cannot run program")
            && message.contains("No such file or directory")) {
          throw new NonExistingProgramException(commandParameters.toString(), io);
        }
      } finally {
        // Close the InputStream

        if (br != null) {
          try {
            br.close();
          } catch (Exception e) {
            this.logger.error("Unable to close br stream");
          }
        }
        if (isr != null) {
          try {
            isr.close();
          } catch (Exception e) {
            this.logger.error("Unable to close isr stream");
          }
        }
        if (is != null) {
          try {
            is.close();
          } catch (Exception e) {
            this.logger.error("Unable to close is stream");
          }
        }

        if (ber != null) {
          try {
            ber.close();
          } catch (Exception e) {
            this.logger.error("Unable to close ber stream");
          }
        }
        if (esr != null) {
          try {
            esr.close();
          } catch (Exception e) {
            this.logger.error("Unable to close esr stream");
          }
        }
        if (es != null) {
          try {
            es.close();
          } catch (Exception e) {
            this.logger.error("Unable to close es stream");
          }
        }

      }

    }
  }
}
