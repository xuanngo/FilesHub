package net.xngo.fileshub;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import net.jpountz.xxhash.StreamingXXHash32;
import net.jpountz.xxhash.XXHashFactory;

public class Utils
{
  /**
   * Get all files from a directory and its subdirectories.
   * @param directoryName to be listed
   */
  public ArrayList<File> getAllFiles(String directoryName)
  {
    
    ArrayList<File> allFiles = new ArrayList<File>();
    
    File directory = new File(directoryName);
    
    // Get all the files from a directory
    File[] fList = directory.listFiles();
    
    for (File file : fList)
    {
      if (file.isFile())
      {
          allFiles.add(file);
          return allFiles;
      }
      else if (file.isDirectory())
      {
        getAllFiles(file.getAbsolutePath());
      }
    }
    
    return allFiles;
  }
  
  public String getHash(File file)
  {
    XXHashFactory factory = XXHashFactory.fastestInstance();
    int seed = 0x9747b28c;  // used to initialize the hash value, use whatever
                            // value you want, but always the same
    StreamingXXHash32 hash32 = factory.newStreamingHash32(seed);
  
    try
    {
      byte[] bufferBlock = new byte[8192]; // 8192 bytes
      FileInputStream fileInputStream = new FileInputStream(file);
  
      int read;
      while ((read = fileInputStream.read(bufferBlock))!=-1) 
      {
        hash32.update(bufferBlock, 0, read);
      }
      fileInputStream.close();
  
      return hash32.getValue()+"";
    }
    catch(UnsupportedEncodingException ex)
    {
      System.out.println(ex);
    }
    catch(IOException ex)
    {
      System.out.println(ex);
    }
    
    return null;
  }
}