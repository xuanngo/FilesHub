package net.xngo.fileshub.cmd;

import java.io.File;
import java.util.Set;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import net.xngo.utils.java.io.FileUtils;

@Parameters(commandDescription = "Hash list of files or directories.")
public class CmdHash
{
  public static String name = "hash";
  
  @Parameter(description = "Hash list of files or directories.", variableArity = true)
  public Set<File> addPaths;
  
  public final Set<File> getAllUniqueFiles()
  {
    return FileUtils.listFiles(this.addPaths);
  }    
}
