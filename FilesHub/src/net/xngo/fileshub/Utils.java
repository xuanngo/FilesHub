package net.xngo.fileshub;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import net.jpountz.xxhash.StreamingXXHash32;
import net.jpountz.xxhash.XXHashFactory;
/**
 * 
 * @author Xuan Ngo
 *
 */
public class Utils
{
  /**
   * Get all files from a directory and its sub-directories.
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
  
  /**
   * Get the hash(ID) of the file.
   * Note: -XXHash32 is chosen because it claims to be fast.
   *       -Check what is the collision rate of XXHash32 algorithm 
   *              because StreamingXXHash32.getValue() return an integer, 
   *              which has a limit of 2,147,483,648.
   * @param file
   * @return      the hash as string
   */
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
  
      return hash32.getValue()+""; // Force to be a string so that if we can change to use another hashing algorithm.
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