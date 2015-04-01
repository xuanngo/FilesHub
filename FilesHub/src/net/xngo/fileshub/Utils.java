package net.xngo.fileshub;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.List;

import net.xngo.utils.java.io.FileUtils;
import net.xngo.utils.java.math.Hash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Xuan Ngo
 *
 */
public class Utils
{
  final static Logger log = LoggerFactory.getLogger(Utils.class);
  
  /**
   * Always get canonical(complete and fully expanded) path of file.
   * Beware of symbolic link.
   * @return Canonical path.
   */
  public static final String getCanonicalPath(File file)
  {
    try
    {
      return file.getCanonicalPath(); // Get the canonical path, the file path in full and expanded.
    }
    catch(IOException e)
    {
      //e.printStackTrace();
      if(e.getMessage().indexOf("Too many levels of symbolic links")!=-1)
      {
        log.warn("Too many levels of symbolic links. Ignore: {}", file.getAbsolutePath(), e);
        
        RuntimeException rException = new RuntimeException(e.getMessage());
        rException.setStackTrace(e.getStackTrace());
        throw rException;
      }
      else
      {
        log.error("", e); // @TODO: Add message. Need to create this conditions.
        e.printStackTrace();
      }
    }
    
    return null; // Return NULL to make this function more brittle so caller will know something is wrong.
  }  
 
  /**
   * Get the hash(ID) of the file.
   * Choose hash algorithm that has low probability of collisions and
   *    its distribution is uniform.
   * @param file
   * @return      the hash as string
   */
  public static final String getHash(File file)
  {
    log.debug("Hashing {} ...", file.getAbsolutePath());
    
    final int fileSizeThreshold = 4194304; // 4MB=4*1024*1024;
    
    // When Config.HASH_FREQUENCY==0, then hash the whole file.
    if(Config.HASH_FREQUENCY==0)
    {
      return Hash.md5(file);
    }
    else
    {
      // Spot hash if the file is bigger than 4MB.
      if(file.length()>fileSizeThreshold)
      {
        return Hash.md5FingerPrint(file, Config.HASH_FREQUENCY);
      }
      else
        return Hash.md5(file);
    }
  }
  
  public static boolean isFileLocked(File f)
  {
    // Try to acquire exclusive lock.
    FileChannel channel = null;
    FileLock lock = null;
    try
    {
      channel = new RandomAccessFile(f, "rw").getChannel();
      lock = channel.tryLock();
      
      if(lock==null)
        return true;

    }
    catch(Exception ex)
    {
      return true;
    }
    
    // Release lock.
    try
    {
      lock.release();
      channel.close();
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
    
    return false;
  }
  
  public static int getUpdateFrequency(int total)
  {
    int partitions = 107;
    int frequency = total/partitions;
    if(frequency<11)
      frequency = 11;
    
    return frequency;
  }
  
  public static String getRAMUsage()
  {
    Runtime runtime = Runtime.getRuntime();
    long usedMemory = runtime.totalMemory()-runtime.freeMemory();
    return String.format("RAM: %s / %s Max=%s", FileUtils.readableSize(usedMemory           ), 
                                                FileUtils.readableSize(runtime.totalMemory()), 
                                                FileUtils.readableSize(runtime.maxMemory()) );
  }
  
  /**
   * @deprecated Not used yet. Need more unit test to remove @deprecated.
   * @param paths
   * @return List of directories as a string.
   */
  public static String getDirsAsString(List<File> paths)
  {
    StringBuilder directories = new StringBuilder();
    String dirSeparator = ", ";
    for(File path: paths)
    {
      if(path.exists())
      {
        File canonicalPath = null;
        try
        {
          canonicalPath = path.getCanonicalFile();
        }
        catch(IOException ex) { ex.printStackTrace(); }
        
        if(canonicalPath.isDirectory())
        {
          if(!canonicalPath.getName().isEmpty())
          {
            directories.append(canonicalPath.getName());
            directories.append(File.separator+dirSeparator);
          }
        }
      }
    }
    
    // Remove the last separator.
    if(directories.length()>0)
    {
      int startpos = directories.lastIndexOf(dirSeparator);
      int endpos   = startpos + dirSeparator.length();
      return directories.replace(startpos, endpos, "").toString();
    }
    else
      return directories.toString();
  }  
}