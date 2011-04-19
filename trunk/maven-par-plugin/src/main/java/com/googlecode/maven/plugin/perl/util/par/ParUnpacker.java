package com.googlecode.maven.plugin.perl.util.par;

import java.io.File;

import org.apache.maven.plugin.logging.Log;

import com.googlecode.maven.plugin.perl.par.exceptions.IncorrectDestinationDirectoryException;
import com.googlecode.maven.plugin.perl.par.exceptions.IncorrectZipFileException;
import com.googlecode.maven.plugin.perl.par.exceptions.UnableToUnzipException;
import com.googlecode.maven.plugin.perl.par.util.PARPacker;
import com.googlecode.maven.plugin.perl.par.util.Unzipper;
import com.googlecode.maven.plugin.perl.util.par.exceptions.UnableToUnpackException;

public class ParUnpacker {

  private Log logger = null;

  /**
     * 
     */
  public ParUnpacker(Log _logger) {
    super();
    this.logger = _logger;
  }

  /**
   * Unpack a par file
   * 
   * @param parFile the par file to unpack
   * @param outputDir directory to extract par file content into
   */
  public void unpack(File parFile, File outputDir) throws UnableToUnpackException {

    this.unpack(parFile, false, outputDir);

  }

  /**
   * Unpack a par file
   * 
   * @param parFile the par file to unpack
   * @param outputDir directory to extract par file content into
   */
  public void unpackLibraries(File parFile, File outputDir) throws UnableToUnpackException {
    this.unpack(parFile, true, outputDir);
  }

  /**
   * Unpack a par file
   * 
   * @param parFile the par file to unpack
   * @param outputDir directory to extract par file content into
   */
  private void unpack(File parFile, boolean unpackLibOnly, File outputDir)
      throws UnableToUnpackException {

    if (parFile == null) {
      if (this.getLogger() != null && this.getLogger().isErrorEnabled()) {
        this.getLogger().error("No par file provided.");
      }
      throw new UnableToUnpackException("No par file provided.");
    }

    if (outputDir == null) {
      if (this.getLogger() != null && this.getLogger().isErrorEnabled()) {
        this.getLogger().error("No output directory provided.");
      }
      throw new UnableToUnpackException("No output directory provided.");
    }

    // creates output directory if needed

    if (!outputDir.exists()) {

      if (!outputDir.mkdirs()) {
        throw new UnableToUnpackException("Unable to create output directory"+outputDir.getAbsolutePath());
      }

    }

    if (!outputDir.canWrite()) {
      throw new UnableToUnpackException(
          "Unable to create file in the output directory. The directory is no writable"+outputDir.getAbsolutePath());
    }
    try {

      if (this.logger.isDebugEnabled()) {
        this.logger.debug("unpacking    " + parFile.getAbsolutePath() + " to "
            + outputDir.getAbsolutePath());
      }
      Unzipper unzip = new Unzipper();

      if (unpackLibOnly) {
        unzip.unzipResourceToDest(parFile, PARPacker.LIB_INTERNAL_DIR_PATH, outputDir);
      } else {
        unzip.unzipToDest(parFile, outputDir);
      }
    } catch (IncorrectZipFileException e) {
      throw new UnableToUnpackException(e);
    } catch (IncorrectDestinationDirectoryException e) {
      throw new UnableToUnpackException(e);
    } catch (UnableToUnzipException e) {
      throw new UnableToUnpackException(e);
    }

  }

  /**
   * @return the logger
   */
  private Log getLogger() {
    return this.logger;
  }
}
