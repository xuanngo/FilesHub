package net.xngo.fileshub.test.helpers;

import java.io.File;
import java.io.IOException;

import net.xngo.fileshub.Utils;

import org.apache.commons.io.FileUtils;

public class Data
{
  public static File createTempFile(final String affix)
  {
    return Data.createTempFile(affix, null);
  }
  
  public static File createTempFile(final String affix, final File directory)
  {
     return Data.createTempFile(affix, directory, null);
  }
  
  public static File createTempFile(final String affix, final File directory, String content)
  {
    File uniqueFile = null;
    try
    {
      final String prefix = String.format("FHTest_%s_", affix);
      final String suffix = ".tmp";
      uniqueFile = File.createTempFile(prefix, suffix, directory);
      
      if(content==null)
        content = uniqueFile.getName(); 
      FileUtils.writeStringToFile(uniqueFile, content, true);
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    return uniqueFile;
  }
  
  public static void copyFile(File from, File to)
  {
    try
    {
      FileUtils.copyFile(from, to);
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }      
  }
  
  public static File copyFileToDirectory(File from, File toDir)
  {
    File copiedFile = null;
    try
    {
      FileUtils.copyFileToDirectory(from, toDir);
      copiedFile = new File(toDir.getAbsolutePath()+File.separator+from.getName());
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    
    return copiedFile;
  }
  
  
  public static void writeStringToFile(final File file, final String content)
  {
    try
    {
      FileUtils.writeStringToFile(file, content, true);
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
  }
  
  /**
   * Return information of a file with user define title.
   * @param file
   * @param title
   * @return
   */
  public static final String getFileInfo(final File file, final String title)
  {
    return String.format("%s:\n"
        + "\tsize           = %d\n"
        + "\tlast_modified  = %d\n"
        + "\thash           = %s\n"
        + "\tfilename       = %s\n"
        + "\tcanonical_path = %s\n"
        , title
        , file.length(), file.lastModified(), Utils.getHash(file), file.getName(), Utils.getCanonicalPath(file));
  }
  
  public static File moveFileToDirectory(File srcFile, File destDir, boolean createDestDir)
  {
    try
    {
      FileUtils.moveFileToDirectory(srcFile, destDir, createDestDir);
      return new File(destDir.getAbsolutePath()+File.separator+srcFile.getName());
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    return null;
  }

  
  
}
