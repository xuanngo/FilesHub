package net.xngo.fileshub.test.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.lang.StringBuilder;
import java.util.Random;
import java.nio.charset.Charset;


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
  
  public static File createTempFileWithBytes(final String affix, final File directory, byte[] content)
  {
    File uniqueFile = null;
    try
    {
      final String prefix = String.format("FHTest_%s_", affix);
      final String suffix = ".tmp";
      uniqueFile = File.createTempFile(prefix, suffix, directory);
      
      if(content.length==0)
        content = uniqueFile.getName().getBytes(Charset.forName("UTF-8")); 
      FileUtils.writeByteArrayToFile(uniqueFile, content, true);
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    return uniqueFile;
  }
  
  public static File createTempFileWithByte(final String affix, byte[] content)
  {
    return Data.createTempFileWithBytes(affix, null, content);
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

  /*
  public static Path createTempDir(final String dirname)
  {
    Path tempDirPath = Paths.get(System.getProperty("java.io.tmpdir")+File.separator+dirname);
    try
    {
      return Files.createDirectory(tempDirPath);
    }
    catch(IOException ex)
    {
      ex.printStackTrace();
    }
    return null;
  }
  
  */
  public static Path createTempDir()
  {
    return createTempDir("");
  }
  
  public static Path createTempDir(final String prefix)
  {
    try
    {
      return Files.createTempDirectory(prefix);
    }
    catch(IOException ex)
    {
      ex.printStackTrace();
    }
    return null;
  }
  
  public static String getTempDirPath()
  {
    return System.getProperty("java.io.tmpdir");
  }
  
  /**
   * Get random bytes of a given size.
   * @param size Be careful. Big number will cause out of heap memory.
   * @return
   */
  public static byte[] getRandomBytes(int size)
  {
    Random random = new Random(1243);
    byte[] randomBytes = new byte[size];
    random.nextBytes(randomBytes);
    return randomBytes;
  }
}
