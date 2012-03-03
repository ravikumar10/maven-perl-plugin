package com.googlecode.maven.plugin.perl.util;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.googlecode.maven.plugin.perl.util.exceptions.CommandExecutionException;


public interface ICommandRunner {

  void run(String exec, List<String> commandParameters, Map<String, String> env,
      File workingDirectory) throws CommandExecutionException;
}
