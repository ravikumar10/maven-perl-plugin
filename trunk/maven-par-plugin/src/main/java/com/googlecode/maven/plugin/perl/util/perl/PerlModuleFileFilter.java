package com.googlecode.maven.plugin.perl.util.perl;

import java.io.File;
import java.io.FileFilter;

import com.googlecode.maven.plugin.perl.util.constants.PerlConstants;



/**
 * Filter to extract perl modules (returns directory to walk through it.)
 * 
 * @author marguerp Modified by $Author: marguerp $
 * @version $Revision: #2 $
 */
public final class PerlModuleFileFilter implements FileFilter {
  // Selects perl module and directory as package
  public boolean accept(File file) {

    boolean accept = false;
    if (file != null) {
      if (file.isDirectory()) {
        accept = true;
      } else {
        // select file with a pm extension

        String extension = this.getFileExtension(file);
        if (PerlConstants.perlModuleFileExtension.equals(extension)) {
          accept = true;
        }
      }
    }
    return accept;
  }

  /**
   * Get file extension from file name
   * 
   * @param filename name of the file to extract extension from
   * @return
   */
  private String getFileExtension(File file) {

    String filename = file.getName();
    String extension = null;
    if (filename != null) {
      extension = (filename.lastIndexOf('.') == -1) ? "" : filename.substring(filename
          .lastIndexOf('.') + 1, filename.length());
    }

    return extension;
  }
}