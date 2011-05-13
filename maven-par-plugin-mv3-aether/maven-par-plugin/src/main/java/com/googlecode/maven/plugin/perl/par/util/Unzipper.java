/**
 * 
 */
package com.googlecode.maven.plugin.perl.par.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.googlecode.maven.plugin.perl.par.exceptions.IncorrectDestinationDirectoryException;
import com.googlecode.maven.plugin.perl.par.exceptions.IncorrectZipFileException;
import com.googlecode.maven.plugin.perl.par.exceptions.UnableToUnzipException;

/**
 * Class to decompress a zip file.
 * 
 * @author pierre
 */
public class Unzipper {

  private static final List<String> executableExtensions = Arrays.asList("pl", "sh", "py");

  public final void copyInputStream(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    int len;

    while ((len = in.read(buffer)) >= 0) {
      out.write(buffer, 0, len);
    }
    in.close();
    out.close();
  }

  /**
   * Unzip file content in to a destination directory
   * 
   * @param fileToUnzip file to unzip
   * @param outputDir destination directory to unzip contained files into
   * @throws UnableToUnzipException, IncorrectZipFileException,
   *           IncorrectDestinationDirectoryException
   */
  public void unzipToDest(File fileToUnzip, File outputDir) throws UnableToUnzipException,
      IncorrectZipFileException, IncorrectDestinationDirectoryException {
    if (fileToUnzip == null || !fileToUnzip.isFile() || !fileToUnzip.canRead()) {
      throw new IncorrectZipFileException();
    }
    if (outputDir == null || !outputDir.isDirectory() || !outputDir.canWrite()) {
      throw new IncorrectDestinationDirectoryException();
    }

    ZipFile zipFile = null;
    try {
      zipFile = new ZipFile(fileToUnzip);

      Enumeration<? extends ZipEntry> entries = zipFile.entries();

      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();

        if (entry.isDirectory()) { // dealing with a directory
          // Assume directories are stored parents first then children.

          File uncompressEntryFile = new File(outputDir, entry.getName());
          // creates the actual file on the file system
          if (!uncompressEntryFile.exists()) {
            if (!uncompressEntryFile.mkdir()) {
              throw new UnableToUnzipException("Unable to create directory on file system "
                  + uncompressEntryFile.getAbsolutePath());// UnableToUnzipFile
            }
          }

        } else { // dealing with a file
          File uncompressEntryFile = new File(outputDir, entry.getName());
          this.copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(
              new FileOutputStream(uncompressEntryFile)));
          // set file executable if it has an expected extension
         if( executableExtensions.contains(getExtension(uncompressEntryFile))) {
           uncompressEntryFile.setExecutable(true, true);
         }
        }
      }
    } catch (ZipException e) {
      throw new UnableToUnzipException(e);
    } catch (FileNotFoundException e) {
      throw new UnableToUnzipException(e);
    } catch (IOException e) {
      throw new UnableToUnzipException(e);
    } catch (UnableToUnzipException e) {
      throw e;
    } finally {

      if (zipFile != null) {
        try {
          zipFile.close();
        } catch (IOException e) {
          throw new UnableToUnzipException(e);
        }
      }
    }
  }

  /**
   * Unzip file content in to a destination directory
   * 
   * @param fileToUnzip file to unzip
   * @param outputDir destination directory to unzip contained files into
   * @throws UnableToUnzipException, IncorrectZipFileException,
   *           IncorrectDestinationDirectoryException
   */
  public void unzipResourceToDest(File fileToUnzip, String resourceToExtract, File outputDir)
      throws UnableToUnzipException, IncorrectZipFileException,
      IncorrectDestinationDirectoryException {

    if (fileToUnzip == null || !fileToUnzip.isFile() || !fileToUnzip.canRead()) {
      throw new IncorrectZipFileException();
    }
    if (outputDir == null || !outputDir.isDirectory() || !outputDir.canWrite()) {
      throw new IncorrectDestinationDirectoryException();
    }

    ZipFile zipFile = null;
    try {
      zipFile = new ZipFile(fileToUnzip);

      Enumeration<? extends ZipEntry> entries = zipFile.entries();

      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();

        String entryName = entry.getName();

        if (resourceToExtract == null || entryName.startsWith(resourceToExtract)) {
          String fileName = entryName;
          if (resourceToExtract != null && !resourceToExtract.isEmpty()) {
            fileName = fileName.replaceFirst(resourceToExtract, "");
          }
          if (entry.isDirectory()) { // dealing with a directory
            // Assume directories are stored parents first then children.

            File uncompressEntryFile = new File(outputDir, fileName);
            // creates the actual file on the file system
            if (!uncompressEntryFile.exists()) {
              if (!uncompressEntryFile.mkdir()) {
                throw new UnableToUnzipException("Unable to create directory on file system "
                    + uncompressEntryFile.getAbsolutePath());// UnableToUnzipFile
              }
            }

          } else { // dealing with a file
            File uncompressEntryFile = new File(outputDir, fileName);
            this.copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(
                new FileOutputStream(uncompressEntryFile)));
            
            // set file executable if it has an expected extension
            if( executableExtensions.contains(getExtension(uncompressEntryFile))) {
              uncompressEntryFile.setExecutable(true, true);
            }
          }
        }
      }
    } catch (ZipException e) {
      throw new UnableToUnzipException(e);
    } catch (FileNotFoundException e) {
      throw new UnableToUnzipException(e);
    } catch (IOException e) {
      throw new UnableToUnzipException(e);
    } catch (UnableToUnzipException e) {
      throw e;
    } finally {

      if (zipFile != null) {
        try {
          zipFile.close();
        } catch (IOException e) {
          throw new UnableToUnzipException(e);
        }
      }
    }
  }

  /**
   * Retrieves extension for a given file
   * 
   * @param f file object
   * @return the file extension, if it is a file, null otherwise
   */
  public static String getExtension(File f) {
    String ext = null;
    String s = f.getName();
    int i = s.lastIndexOf('.');

    if (f.isFile()) {
      if (i > 0 && i < s.length() - 1) {

        ext = s.substring(i + 1).toLowerCase();
      }
    }
    return ext;
  }

}
