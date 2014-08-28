package net.xngo.fileshub.cmd;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import net.xngo.fileshub.Utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.beust.jcommander.Parameter;

public class Options
{

  @Parameter(names = {"-a", "--add"}, description = "Add list of files or directories.", variableArity = true)
  public Set<File> addPaths;
  
  @Parameter(names = {"-u", "--update"}, description = "Update database: check if files have changes, not available files.")
  public boolean update;  
  
  @Parameter(names = {"-d", "--duplicate"}, description = "Mark 2 files that are duplicate.", arity = 2) // Use case: Both files have different hash.
  public List<File> duplicateFiles;    
  
  public final Set<File> getAllUniqueFiles()
  {
    Set<File> listOfAllUniqueFiles = new HashSet<File>();
    
    for(File path: this.addPaths)
    {
      if(path.exists())
      {
        try
        {
          File canonicalFilePath = path.getCanonicalFile(); // Get all input files/directories paths as canonical to ensure that there will be no duplicate.
          if(canonicalFilePath.isFile())
          {
            listOfAllUniqueFiles.add(canonicalFilePath);
          }
          else
          {// It is a directory.
            Collection<File> filesList = FileUtils.listFiles(canonicalFilePath, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
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
