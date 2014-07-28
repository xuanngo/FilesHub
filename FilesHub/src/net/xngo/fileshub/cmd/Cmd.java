package net.xngo.fileshub.cmd;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.io.File;
import java.io.IOException;

import net.xngo.fileshub.Utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.beust.jcommander.Parameter;


public class Cmd
{
  @Parameter(names = {"-a", "--add"}, description = "Add list of files or directories.", required = true, variableArity = true)
  private Set<File> paths;
  
  
  public final Set<File> getAllUniqueFiles()
  {
    Set<File> listOfAllUniqueFiles = new HashSet<File>();
    
    for(File path: paths)
    {
      if(path.exists())
      {
        try
        {
          File canonicalPath = path.getCanonicalFile(); // Ensure that will be no duplicates.
          if(canonicalPath.isFile())
          {
            listOfAllUniqueFiles.add(canonicalPath);
          }
          else
          {// It is a directory.
            Collection<File> filesList = FileUtils.listFiles(canonicalPath, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            listOfAllUniqueFiles.addAll(filesList);
          }
        }
        catch(IOException e)
        {
          e.printStackTrace();
        }
        
      }
      else
      {
        System.out.println(String.format("[Warning] -> [%s] doesn't exist.", Utils.getCanonicalPath(path)));
      }
    }
    
    return listOfAllUniqueFiles;
  }
}
