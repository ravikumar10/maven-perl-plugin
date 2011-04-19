/**
 * 
 */
package com.googlecode.maven.plugin.perl.util.maven;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.DefaultMavenProjectBuilder;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;

import com.googlecode.maven.plugin.perl.par.exceptions.IncorrectDestinationDirectoryException;
import com.googlecode.maven.plugin.perl.par.exceptions.IncorrectZipFileException;
import com.googlecode.maven.plugin.perl.util.constants.PerlConstants;
import com.googlecode.maven.plugin.perl.util.maven.exceptions.UnableToCopyFileException;
import com.googlecode.maven.plugin.perl.util.par.ParUnpacker;
import com.googlecode.maven.plugin.perl.util.par.exceptions.UnableToUnpackException;

/**
 * Class to handler maven perl dependencies
 * 
 * @author marguerp Modified by $Author: marguerp $
 * @version $Revision: #10 $
 */
public class MavenPerlDependencyManager {

  private static final String ERROR_COPYING_LIBS_MSG = "Error copying libs";

  private Log logger = null;

  /**
   * Used to build a maven projects from artifacts in the remote repository.
   */
  private DefaultMavenProjectBuilder projectBuilder;

  /**
   * Location of the local repository.
   */
  private org.apache.maven.artifact.repository.ArtifactRepository localRepository;

  /**
   * List of Remote Repositories used by the resolver
   */
  private List remoteRepositories;

  /** @component */
  private org.apache.maven.artifact.factory.ArtifactFactory artifactFactory;

  /** @component */
  private org.apache.maven.artifact.resolver.ArtifactResolver resolver;

  public MavenPerlDependencyManager(Log _logger, DefaultMavenProjectBuilder _projectBuilder,
      ArtifactRepository _localRepository, List _remoteRepositories,
      ArtifactFactory _artifactFactory, ArtifactResolver _resolver) {
    super();
    this.logger = _logger;
    this.projectBuilder = _projectBuilder;
    this.localRepository = _localRepository;
    this.remoteRepositories = _remoteRepositories;
    this.artifactFactory = _artifactFactory;
    this.resolver = _resolver;
  }

  /**
   * Extracts package dependencies or get path to system dependencies Libraries
   * 
   * @return list of directories containing lib (system and extracted)
   * @throws MojoExecutionException
   */
  public Collection<String> extractProjectDependencies(MavenProject mavenProject,
      File liboutputDirectory, boolean isTestMode, File testliboutputDirectory)
      throws MojoExecutionException {

    MavenDependencies deps = this.getProjectDependencies(mavenProject, isTestMode);

    Collection<String> directories = null;
    if (deps != null) {

      try {
        // get system library path
        directories = deps.getDependencySystemPaths();
        if (directories == null) {
          directories = new ArrayList<String>();
        }

        // extracts dependency artifacts
        Collection <Artifact> artifacts = deps.getArtifacts();

        ParUnpacker unpacker = new ParUnpacker(this.getLogger());
        if (artifacts != null && !artifacts.isEmpty()) {
          for (Artifact art : artifacts) {
            // if no classifier, it is library file only. Otherwise it is a packaged script
            File artifactFile = art.getFile();
            if (art.getClassifier() == null) {

              unpacker.unpackLibraries(artifactFile, liboutputDirectory);
            } else {
              // copy script script
              // the real name of the script is in the classifier attribute
              File exportedScript = new File(liboutputDirectory, art.getClassifier());
              this.copyPackagedScript(artifactFile, exportedScript);
              // insures it is executable
              exportedScript.setExecutable(true, true);
            }
          }
          directories.add(liboutputDirectory.getAbsolutePath()); // + File.separator +
          // PARPacker.LIB_INTERNAL_DIR_PATH);
        }

        // extract test dependencies if required
        Collection<Artifact> testArtifacts = deps.getTestArtifacts();

        if (isTestMode && testArtifacts != null && !testArtifacts.isEmpty()) {
          for (Artifact art : testArtifacts) {
            // if no classifier, it is library file only. Otherwise, it is a packaged script
            File artifactFile = art.getFile();
            if (art.getClassifier() == null) {
              unpacker.unpackLibraries(artifactFile, testliboutputDirectory);
            } else {
              // copy script script
              File exportedScript = new File(testliboutputDirectory, art.getClassifier());
              this.copyPackagedScript(artifactFile, exportedScript);
              // insures it is executable
              exportedScript.setExecutable(true, true);
            }
          }
          directories.add(liboutputDirectory.getAbsolutePath()); // + File.separator +
          // PARPacker.LIB_INTERNAL_DIR_PATH);
        }
      } catch (UnableToUnpackException e) {
        throw new MojoExecutionException("", e);
      }

    }

    return directories;
  }

  /**
   * Extracts package dependencies or get path to system dependencies Libraries are available as
   * system dependencies
   * 
   * @return list of directories containing lib
   * @throws MojoExecutionException
   */
  private MavenDependencies getProjectDependencies(MavenProject mavenProject, boolean isTestMode)
      throws MojoExecutionException {

    MavenDependencies currentDeps = null;

    try {

      Set<Artifact> artifacts = mavenProject.getArtifacts();
      if (this.logger.isInfoEnabled()) {
        this.logger.info(" Project " + mavenProject.getName());
      }

      // foreach dependency, decompress the file in the lib directory and do the same thing for
      // transitive dependencies recursively

      if (artifacts != null && !artifacts.isEmpty()) {

        for (Artifact depArtifact : artifacts) {
          try {
            // get all dependencies and recreates mavenProject object to deal with it

            MavenProject pomProject = this.projectBuilder.buildFromRepository(depArtifact,
                this.remoteRepositories, this.localRepository);

            if (!depArtifact.isOptional()) {
              if (this.logger.isInfoEnabled()) {
                this.logger.info("Perl artifact as Dependency " + depArtifact.getArtifactId()+" type "+depArtifact.getType());
              }

              if (PerlConstants.PAR_MAVEN_PROJECT_TYPE.equals(depArtifact.getType())) {
                if (currentDeps == null) {
                  currentDeps = new MavenDependencies(isTestMode);
                }
                boolean alreadyKnown = !currentDeps.addDependency(depArtifact);

                // IF RUNTIME - decompress par archive
                // SYSTEM dependency - stores path to the lib directory
                if (!alreadyKnown) {
                  // fixed for circular dependencies
                  MavenDependencies subDeps = this.getProjectDependencies(pomProject, isTestMode);
                  if (subDeps != null) {
                    currentDeps.addAllDependencies(subDeps);
                  } else {
                    if (this.logger.isDebugEnabled()) {
                    this.logger.debug(depArtifact.getArtifactId()+" no sub dependency to add");
                    }
                  }
                } else {
                  if (this.logger.isDebugEnabled()) {
                  this.logger.debug(" The artifact "+depArtifact.getArtifactId()+" is already known.");
                  }
                }

              }

            }

          } catch (ProjectBuildingException e) {

            // TODO change to proper exception
            throw new MojoExecutionException("Unable to build project: "
                + depArtifact.getDependencyConflictId(), e);
          }
        }
      }

        for (Dependency dep : (List<Dependency>) mavenProject.getDependencies()) {

          if (this.logger.isInfoEnabled()) {
          this.logger.info(" dependency   " + dep);
          }
          // DEAL with dependencies

          Artifact pomArtifact = this.artifactFactory.createArtifactWithClassifier(
              dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), dep.getType(), dep
                  .getClassifier());

          this.resolver.resolve(pomArtifact, this.remoteRepositories, this.localRepository);

          try {
            // get all dependencies and recreates mavenProject object to deal with it

            MavenProject pomProject = this.projectBuilder.buildFromRepository(pomArtifact,
                this.remoteRepositories, this.localRepository);

            if (!pomArtifact.isOptional()) {

              if (PerlConstants.PAR_MAVEN_PROJECT_TYPE.equals(pomArtifact.getType())) {
                if (currentDeps == null) {
                  currentDeps = new MavenDependencies(isTestMode);
                }
                boolean alreadyKnown = !currentDeps.addDependency(pomArtifact);

                if (!alreadyKnown) {
                  // fixed for circular dependencies
                  MavenDependencies subDeps = this.getProjectDependencies(pomProject, isTestMode);
                  if (subDeps != null) {
                    currentDeps.addAllDependencies(subDeps);
                  } else {
                    if (logger.isDebugEnabled()) {
                    this.logger.debug(" no sub dependency to add");
                    }
                  }
                }else {
                  if (logger.isDebugEnabled()) {
                  this.logger.debug(" Dependency already known");
                  }
                }
              }

            }

          } catch (ProjectBuildingException e) {
            throw new MojoExecutionException("Unable to build project: "
                + pomArtifact.getDependencyConflictId(), e);
          }

        
      }

    } catch (IncorrectZipFileException e) {
      throw new MojoExecutionException(ERROR_COPYING_LIBS_MSG, e);
    } catch (IncorrectDestinationDirectoryException e) {
      throw new MojoExecutionException(ERROR_COPYING_LIBS_MSG, e);
    } catch (ArtifactResolutionException e) {
      throw new MojoExecutionException(ERROR_COPYING_LIBS_MSG, e);
    } catch (ArtifactNotFoundException e) {
      throw new MojoExecutionException(ERROR_COPYING_LIBS_MSG, e);
    }

    return currentDeps;
  }

  /**
   * Copy perl modules to a given directory
   * 
   * @throws MojoExecutionException
   */
  public void copyProjectModules(File moduleDir, File buildModuleDir) throws MojoExecutionException {

    try {

      if (moduleDir != null && moduleDir.exists()) {

        if (this.logger.isInfoEnabled()) {
          this.logger.info("Copying modules to " + buildModuleDir.getAbsolutePath());
        }

        this.copy(moduleDir, buildModuleDir);
      }
    } catch (UnableToCopyFileException e) {
      throw new MojoExecutionException("Error copying perl modules ", e);
    }

  }

  /**
   * @param scriptFile
   * @param destinationDir
   * @throws MojoExecutionException
   */
  public void copyPackagedScript(File scriptFile, File destinationDir)
      throws MojoExecutionException {

    try {

      if (destinationDir != null
          && ((destinationDir.isDirectory() && destinationDir.exists()) || !destinationDir.exists())) {

        if (this.logger.isInfoEnabled()) {
          this.logger.info("Copying script to " + destinationDir.getAbsolutePath());
        }

        this.copy(scriptFile, destinationDir);
      } else {
        if (this.logger.isInfoEnabled()) {
          this.logger
              .info("no destination directory. Not Copying script " + (destinationDir != null ? destinationDir
                  .getAbsolutePath()
                  : ""));
        }
      }
    } catch (UnableToCopyFileException e) {
      throw new MojoExecutionException("Error copying script ", e);
    }

  }

  /**
   * Copy a File object (file or directory )
   * 
   * @param origin file object to copy
   * @param destination destination of the copy
   * @throws UnableToCopyFileException if unable to copy data
   */
  private void copy(File origin, File destination) throws UnableToCopyFileException {

    try {
      if (origin != null && origin.exists()) {
        if (origin.isDirectory()) {

          if (destination == null) {// || !buildModuleDir.exists()) {
            throw new IOException("Destination does not exist.");
          }
          FileUtils.copyDirectory(origin, destination, true);
        } else {
          if (destination.isDirectory()) {
            FileUtils.copyFileToDirectory(origin, destination, true);
          } else {
            FileUtils.copyFile(origin, destination, true);
          }

        }
      }
    } catch (IOException e) {
      throw new UnableToCopyFileException("Error copying file ", e);
    }

  }

  private final class MavenDependencies {

    private final ArtifactFilter runtime_filter = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);

    private final ArtifactFilter system_filter = new ScopeArtifactFilter(Artifact.SCOPE_SYSTEM);

    private final ArtifactFilter test_filter = new ScopeArtifactFilter(Artifact.SCOPE_TEST);

    private final ArtifactFilter provided_filter = new ScopeArtifactFilter(Artifact.SCOPE_PROVIDED);

    // private Set<Artifact> artifacts = null;
    //
    // private Set<Artifact> testArtifacts = null;

    private Map<String, Artifact> artifacts = null;

    private Map<String, Artifact> testArtifacts = null;

    private Set<String> dependencySystemPath = null;

    private boolean testMode = false;

    private static final String GROUPID_ARTID_SEPARATOR = "-IDSeq-";
    
    private static final String ARTID_CLASSIFIER_SEPARATOR = "-IDCLASSSeq-";
  

    public MavenDependencies() {
      super();

    }

    public MavenDependencies(boolean isTestMode) {
      super();
      this.testMode = isTestMode;

    }

    /**
     * @return the artifacts
     */
    protected Collection<Artifact> getArtifacts() {
      Collection<Artifact> artifactList = null;

      if (this.artifacts != null) {
        artifactList = new ArrayList<Artifact>(this.artifacts.values());
      }
      return artifactList;
    }

    /**
     * @return the testArtifacts
     */
    protected Collection<Artifact> getTestArtifacts() {
      Collection<Artifact> testArtifactList = null;

      if (this.testArtifacts != null) {
        testArtifactList = new ArrayList<Artifact>(this.testArtifacts.values());
      }

      return testArtifactList;
    }

    /**
     * @return the dependencySystemPath
     */
    protected Set<String> getDependencySystemPaths() {
      return this.dependencySystemPath;
    }

    /**
     * Add dependency to the current object check for test dependency, store differently from
     * runtime and default dependencies TODO CIRCULAR DEPENDENCIES Insures to take the lasted
     * version
     * 
     * @param dependencyArtifact
     */
    protected boolean addDependency(Artifact dependencyArtifact) {

      boolean added = false;
      if (dependencyArtifact != null) {

        if (dependencyArtifact.getScope() == null
            || (this.runtime_filter.include(dependencyArtifact))) {
          if (this.artifacts == null) {
            this.artifacts = new HashMap<String, Artifact>();
          }
          added = this.addToArtifactList(dependencyArtifact, this.artifacts );

        } else {
          if (this.testMode
              && (this.test_filter.include(dependencyArtifact) || this.provided_filter
                  .include(dependencyArtifact))) {
            if (this.testArtifacts == null) {
              this.testArtifacts = new HashMap<String, Artifact>();
            }
            added = this.addToArtifactList(dependencyArtifact, this.testArtifacts  );

          } else {
            if (this.system_filter.include(dependencyArtifact)) {

              if (this.dependencySystemPath == null) {
                this.dependencySystemPath = new HashSet<String>();
              }

              File dependencyFile = dependencyArtifact.getFile();
              if (dependencyFile.isFile()) {// maven only accepts existing file as value - not
                // directory -, get the
                // parent directory to have the real lib directory
                dependencyFile = dependencyFile.getParentFile();
              }
              added = this.dependencySystemPath.add(dependencyFile.getAbsolutePath());

            }
          }
        }
        if (logger.isDebugEnabled()) {
          logger.debug("artifacts " + artifacts);
        }
      } else {
        if (logger.isDebugEnabled()) {
          logger.debug("no artifact provided ");
        }
      }
      return added;
    }

    /**
     * @param dependencyArtifact
     * @param added
     * @return
     */
    private boolean addToArtifactList(Artifact dependencyArtifact, Map<String, Artifact>artifactList) {
      
      boolean added =false;
      String classifier = dependencyArtifact.getClassifier();
      if(classifier == null ) {
        classifier = "";
      }
      String key = dependencyArtifact.getGroupId() + GROUPID_ARTID_SEPARATOR
          + dependencyArtifact.getArtifactId()+ARTID_CLASSIFIER_SEPARATOR+classifier;
      Artifact knownArtifact = artifactList.get(key);
      if (knownArtifact == null) {// add new artifact
        artifactList.put(key, dependencyArtifact);
        added = true;
      } else {
        // the artifact is already known
        // check version

        // The default specification should be composed as follows:
        //
        // <major>.<minor>.<revision>([ -<qualifier> ] | [ -<build> ])
        //
        // where:
        //
        // * the qualifier section is optional (and is SNAPSHOT, alpha-1, alpha-2)
        // * the build section is optional (and increments starting at 1 if specified)
        // * any '0' build or revision elements can be omitted.
        // * only one of build and qualifier can be given (note that the timestamped qualifier
        // includes a build number, but this is not the same)
        // * the build number is for those that repackage the original artifact (eg, as is often
        // done with rpms)
        //
        // For ordering, the following is done in order until an element is found that are not
        // equal:
        //
        // * numerical comparison of major version
        // * numerical comparison of minor version
        // * if revision does not exist, add ".0" for comparison purposes
        // * numerical comparison of revision
        // * if qualifier does not exist, it is newer than if it does
        // * case-insensitive string comparison of qualifier
        // o this ensures timestamps are correctly ordered, and SNAPSHOT is newer than an
        // equivalent timestamp
        // o this also ensures that beta comes after alpha, as does rc
        // * if no qualifier, and build does not exist, add "-0" for comparison purposes
        // * numerical comparison of build

      }
      return added;
    }

    /**
     * Adds dependencies from another MavenDependencies object
     * 
     * @param dependencies dependency object to add to the current one
     */
    protected void addAllDependencies(MavenDependencies dependencies) {

      if (dependencies != null) {
        Collection<Artifact> artifactsToAdd = dependencies.getArtifacts();
        if (artifactsToAdd != null && !artifactsToAdd.isEmpty()) {
          if (this.artifacts == null) {
            this.artifacts = new HashMap<String, Artifact>();
          }
          if (this.testArtifacts == null) {
            this.testArtifacts = new HashMap<String,Artifact>();
          }
            
            for (Artifact art  : artifactsToAdd) {
              this.addToArtifactList(art, this.artifacts  );
            }
           
         
        } else {
          if (logger.isDebugEnabled()) {
            logger.debug("no artifact to add ");
          }
        }

        Set<String> pathToAdd = dependencies.getDependencySystemPaths();
        if (pathToAdd != null && !pathToAdd.isEmpty()) {
          if (this.dependencySystemPath == null) {
            this.dependencySystemPath = new HashSet<String>();
          }

          this.getDependencySystemPaths().addAll(pathToAdd);
        }
      }

    }

  }

  /**
   * @return the logger
   */
  protected Log getLogger() {
    return this.logger;
  }

}
