package net.xngo.fileshub.test.helpers;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class Data
{
  public static File createTempFile(final String affix)
  {
    return Data.createTempFile(affix, null);
  }
  
  public static File createTempFile(final String affix, final File directory)
  {
    File uniqueFile = null;
    try
    {
      final String prefix = String.format("FHTest_%s_", affix);
      final String suffix = ".tmp";
      uniqueFile = File.createTempFile(prefix, suffix, directory);
      FileUtils.writeStringToFile(uniqueFile, uniqueFile.getName(), true);
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
  

  
}
