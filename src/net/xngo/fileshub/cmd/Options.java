package net.xngo.fileshub.cmd;

import java.io.File;
import java.util.List;

import com.beust.jcommander.Parameter;

public class Options
{

  @Parameter(names = {"-a", "--add"}, description = "Add list of files or directories.", variableArity = true)
  public List<File> addPaths;
  
  @Parameter(names = {"-u", "--update"}, description = "Update database if files have changed and return missing files.")
  public boolean update;  
  
  @Parameter(names = {"-d", "--duplicate"}, description = "Mark 2 files as duplicate.", arity = 2) // Use case: Mark 2 files as duplicate regardless of their content.
  public List<File> duplicateFiles;
  
  @Parameter(names = {"-g", "--upgrade"}, description = "Upgrade the database.")
  public boolean upgrade;    
}
