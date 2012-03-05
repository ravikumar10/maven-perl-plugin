/**
 * 
 */
package com.googlecode.maven.plugin.perl.util;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.shell.BourneShell;

import com.googlecode.maven.plugin.perl.util.constants.CLIConstants;
import com.googlecode.maven.plugin.perl.util.exceptions.CommandExecutionException;
import com.googlecode.maven.plugin.perl.util.exceptions.ErrorCodeCommandExecutionException;
import com.googlecode.maven.plugin.perl.util.exceptions.IncorrectCommandException;
import com.googlecode.maven.plugin.perl.util.exceptions.NonExistingProgramException;
import com.googlecode.maven.plugin.perl.util.exceptions.UnableToHandlerOutputException;


/**
 * Abstract class for running command line with handling of standard and error output
 * 
 * @author marguerp Modified by $Author: marguerp $
 * @version $Revision: #4 $
 */
public abstract class AMavenCLIRunner implements ICommandRunner {

  private static final int EXEC_NOT_FOUND_ERROR_CODE = 126;

  /**
   * 
   */
  public AMavenCLIRunner() {
    super();
  }

  /*
   * (non-Javadoc)
   * @see com.googlecode.maven.plugin.perl.util.ICommandRunner#run(java.lang.String, java.util.List,
   * java.util.Map, java.io.File)
   */
  @Override
  public void run(String exec, List<String> commandParameters, Map<String, String> env,
      File workingDirectory) throws CommandExecutionException {
    if (exec == null || exec.isEmpty()) {
      throw new IncorrectCommandException("No command provided");
    }

    try {

      this.logDebug("Running command: ", exec);

      Commandline cl = new Commandline(exec, new BourneShell());

      if (workingDirectory != null && workingDirectory.exists()) {
        cl.setWorkingDirectory(workingDirectory);

        // Add working directory as main location for executables
        if (env != null && !env.isEmpty()) {
          String pathEnvVar = env.get(CLIConstants.PATH_ENV_VAR);

          StringBuilder buf = new StringBuilder(pathEnvVar);

          buf.append(File.pathSeparator);
          buf.append(workingDirectory.getAbsolutePath());
          pathEnvVar = buf.toString();
          env.put(CLIConstants.PATH_ENV_VAR, pathEnvVar);
        }

      }
      // if environment variable provided, add them
      if (env != null && !env.isEmpty()) {

        for (Entry<String, String> entry : env.entrySet()) {

          cl.addEnvironment(entry.getKey(), entry.getValue());
        }

      }

      // if arguments provided, add to the command line
      if (commandParameters != null && !commandParameters.isEmpty()) {
        String[] argvs = commandParameters.toArray(new String[commandParameters.size()]);
        this.logDebug("argvs ", commandParameters);

        cl.addArguments(argvs);

      }

      InputStream input = null;
      StreamConsumer output = new CommandLineUtils.StringStreamConsumer() {
        @Override
        public void consumeLine(String line) {

          try {
            AMavenCLIRunner.this.handlerStandardOutput(line);
          } catch (UnableToHandlerOutputException e) {
            logError(e, "Error during standard output handling.");
            throw new RuntimeException(e);
          }
        }

      };
      StreamConsumer error = new CommandLineUtils.StringStreamConsumer() {
        @Override
        public void consumeLine(java.lang.String line) {
          try {
            AMavenCLIRunner.this.handlerErrorOutput(line);
          } catch (UnableToHandlerOutputException e) {
            logError(e, "Error during error output handling.");
            throw new RuntimeException(e);
          }
        }

      };
      int returnValue = CommandLineUtils.executeCommandLine(cl, input, output, error);

      if (returnValue != 0) {
        this.logError("Error during execution", Integer.valueOf(returnValue));
        if (returnValue == EXEC_NOT_FOUND_ERROR_CODE) {
          throw new NonExistingProgramException();
        }
        /*
         * TODO: logError() messages in StreamConsumer's above may not reach
         * console before this exception explodes: added cl.toString() below so
         * user sees full shell command that failed, can then reproduce
         * manually, fix the issue, etc. To reproduce 120304 error which
         * motivated this change, delete any TestRunner.pl in project workspace
         * and run 'mvn perl:test'.
         */
        throw new ErrorCodeCommandExecutionException(cl.toString()
            + " (returned " + returnValue + ")");
      }

    } catch (CommandLineException e) {
      this.logError(e, "Unable to run command: ", exec, " ", commandParameters);

      throw new CommandExecutionException(e);

    }

  }

  /**
   * abstract method to handler a line of the standard output
   * 
   * @param line a string from the standard output
   * @throws UnableToHandlerOutputException if an error occurs during the handling of the output
   */
  protected abstract void handlerStandardOutput(String line) throws UnableToHandlerOutputException;

  /**
   * abstract method to handler a line of the error output
   * 
   * @param line a string from the error output
   * @throws UnableToHandlerOutputException if an error occurs during the handling of the output
   */
  protected abstract void handlerErrorOutput(String line) throws UnableToHandlerOutputException;

  /**
   * abstract method for logging debug messages
   * 
   * @param msgs list of messages
   */
  protected abstract void logDebug(Object... msgs);

  /**
   * abstract method for logging info messages
   * 
   * @param msgs list of messages
   */
  protected abstract void logInfo(Object... msgs);

  /**
   * abstract method for logging error messages
   * 
   * @param msgs list of messages
   */
  protected abstract void logError(Object... msgs);

  /**
   * abstract method for logging error messages
   * 
   * @param exception to log
   * @param msgs list of messages
   */
  protected abstract void logError(Exception e, Object... msgs);

}

/*
 * Unix error code perl -e 'printf "%i\t%s\n", (${!}=$_)x2 for 0..131' 0 1 Opération non permise 2
 * Aucun fichier ou répertoire de ce type 3 Aucun processus de ce type 4 Appel système interrompu 5
 * Erreur d'entrée/sortie 6 Aucun périphérique ou adresse 7 Liste d'arguments trop longue 8 Erreur
 * de format pour exec() 9 Mauvais descripteur de fichier 10 Aucun processus enfant 11 Ressource
 * temporairement non disponible 12 Ne peut allouer de la mémoire 13 Permission non accordée 14
 * Mauvaise adresse 15 Bloc de périphérique requis 16 Périphérique ou ressource occupé 17 Le fichier
 * existe. 18 Lien croisé de périphéque invalide 19 Aucun périphérique de ce type 20 N'est pas un
 * répertoire 21 est un répertoire 22 Argument invalide 23 Trop de fichiers ouverts dans le système
 * 24 Trop de fichiers ouverts 25 Ioctl() inappropré pour un périphérique 26 Fichier texte occupé 27
 * Fichier trop gros 28 Aucun espace disponible sur le périphérique 29 Repérage illégal 30 Système
 * de fichiers accessible en lecture seulement 31 Trop de liens 32 Relais brisé (pipe) 33 L'argument
 * numérique est hors du domaine. 34 Le résultat numérique est en dehors de l'intervalle. 35 Blocage
 * évité des accès aux ressources 36 Nom de fichier trop long 37 Aucun verrou disponible 38 Fonction
 * non implantée 39 Le répertoire n'est pas vide. 40 Trop de niveaux de liens symboliques 41 Erreur
 * inconnue 41 42 Aucun message du type désiré 43 Identificateur éliminé 44 Numéro de canal en
 * dehors des limites 45 Niveau 2 non synchronisé 46 Niveau 3 en halte 47 Niveau 3 réinitialisé 48
 * Numéro du lien hors intervalle 49 Pilote du protocole n'est pas attaché 50 Aucune structure CSI
 * disponible 51 Niveau 2 en halte 52 Échange invalide 53 Descripteur de requête invalide 54
 * L'échangeur est plein. 55 Aucune « anode » disponible 56 Code de requête invalide 57 Dalot
 * invalide 58 Erreur inconnue 58 59 Mauvais format du fichier de fontes 60 Le périphérique n'est
 * pas de type « stream ». 61 Aucune donnée disponible 62 Expiration de la minuterie 63 Aucune autre
 * ressource de type streams disponible 64 La machine cible n'est pas sur le réseau. 65 Le package
 * n'est pas installé. 66 L'objet est télé-accessible. 67 Le lien a été endommagé. 68 Erreur
 * d'annonce 69 Erreur srmount() 70 Erreur de communication lors de la transmission 71 Erreur de
 * protocole 72 Tentative de connexion par de multiples noeuds de relais 73 Erreur spécifique à «
 * RFS » 74 Message invalide 75 Valeur trop grande pour le type défini de données 76 Le nom n'est
 * pas unique sur le réseau. 77 Le descripteur du fichier est dans un mauvais état. 78 L'adresse de
 * l'hôte cible a été modifiée. 79 Ne peut accéder à la librairie partagée demandée 80 Accès d'une
 * librairie partagée qui est corrompue 81 La section .lib dans a.out est corrompue. 82 Tentative
 * d'édition de liens à partir de trop de librairies partagées 83 Ne peut exécuter une librairie
 * partagée directement 84 Chaîne multi-octets ou étendue de caractères invalide ou incomplète 85
 * Appel système interrompu, il aurait dû être relancé 86 Erreur de relais de type streams 87 Trop
 * d'usagers 88 Opération de type socket sur un type non socket 89 Adresse de destination requise 90
 * Message trop long 91 Mauvais type pour un socket de protocole 92 Protocole non disponible 93
 * Protocole non supporté 94 Type de socket non supporté 95 Opération non supportée 96 Famille de
 * protocoles non supportée 97 Famille d'adresses non supportée par le protocole 98 Adresse déjà
 * utilisée 99 Ne peut attribuer l'adresse demandée 100 Le réseau ne fonctionne pas. 101 Le réseau
 * n'est pas accessible. 102 Le réseau a rompu la connexion lors de la réinitialisation. 103 Le
 * logiciel a provoqué l'abandon de la connexion. 104 Connexion ré-initialisée par le correspondant
 * 105 Aucun espace tampon disponible 106 Noeud final de transport déjà connecté 107 Noeud final de
 * transport n'est pas connecté 108 Ne peut transmettre suite à la fermeture du noeud final de
 * transport 109 Trop de références: ne peut segmenter 110 Connexion terminée par expiration du
 * délai d'attente 111 Connexion refusée 112 L'hôte cible est arrêté ou en panne. 113 Aucun chemin
 * d'accès pour atteindre l'hôte cible 114 Opération déjà en cours 115 Opération maintenant en cours
 * 116 Panne d'accès au fichier NFS 117 La structure a besoin d'un nettoyage. 118 Aucun fichier de
 * type « XENIX named » 119 Aucun sémaphore XENIX disponible 120 est un type de fichier nommé
 * (named) 121 Erreur d'entrée/sortie sur l'hôte cible 122 Débordement du quota d'espace disque 123
 * Aucun medium trouvé 124 Mauvais type de medium 125 Opération annulée 126 Required key not
 * available 127 Key has expired
 */
