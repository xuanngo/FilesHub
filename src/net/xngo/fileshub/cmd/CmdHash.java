package net.xngo.fileshub.cmd;

import java.io.File;
import java.util.List;
import java.util.Set;

import net.xngo.utils.java.io.FileUtils;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Hash list of files or directories.")
public class CmdHash
{
  public static String name = "hash";
  
  @Parameter(description = "Hash list of files or directories.", variableArity = true)
  public List<File> paths;
  
  public final Set<File> getAllUniqueFiles()
  {
    return FileUtils.listFiles(this.paths);
  }    
}
