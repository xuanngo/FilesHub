package net.xngo.fileshub;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import net.jpountz.xxhash.StreamingXXHash32;
import net.jpountz.xxhash.XXHashFactory;
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
    log.debug("Hashing {}", file.getAbsolutePath());
    
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
  
  /**
   * Source: http://examples.javacodegeeks.com/core-java/io/file/4-ways-to-copy-file-in-java/
   */
  public static void copyFileUsingFileStreams(File source, File dest)
  {
    try 
    {
      InputStream input = new FileInputStream(source);
      OutputStream output = new FileOutputStream(dest);
      byte[] buf = new byte[8192];
      int bytesRead;
      while ((bytesRead = input.read(buf)) > 0) 
      {
        output.write(buf, 0, bytesRead);
      }
      
      input.close();
      output.close();      
    } 
    catch(FileNotFoundException ex)
    {
      ex.printStackTrace();
    }
    catch(IOException ex)
    {
      ex.printStackTrace();
    }      
  }
  
  
  /**
   * Source: http://examples.javacodegeeks.com/core-java/io/file/4-ways-to-copy-file-in-java/
   */
  public static void copyFileUsingFileChannels(File source, File dest)
  {
    FileChannel inputChannel = null;
    FileChannel outputChannel = null;
    try 
    {
      inputChannel = new FileInputStream(source).getChannel();
      outputChannel = new FileOutputStream(dest).getChannel();
      outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
      
      inputChannel.close();
      outputChannel.close();      
    }
    catch(FileNotFoundException ex)
    {
      ex.printStackTrace();
    }
    catch(IOException ex)
    {
      ex.printStackTrace();
    }     
  }
  
  /**
   * copyNhash() combine the copying and the hashing process at the same time so that it only fetches data once from hard drive.
   * TODO Modify code to use FileChannel class. It is much faster.
   * 
   * @param source
   * @param dest
   * @return
   */
  public String copyNhash(File source, File dest)
  {
    XXHashFactory factory = XXHashFactory.fastestInstance();
    int seed = 0x9747b28c;  // used to initialize the hash value, use whatever
                            // value you want, but always the same
    StreamingXXHash32 hash32 = factory.newStreamingHash32(seed);
  
    try
    {
      byte[] bufferBlock = new byte[8192]; // 8192 bytes
      FileInputStream fileInputStream = new FileInputStream(source);
      OutputStream fileOutputStream = new FileOutputStream(dest);
  
      int read;
      while ((read = fileInputStream.read(bufferBlock))!=-1) 
      {
        hash32.update(bufferBlock, 0, read);  // Hash
        
        fileOutputStream.write(bufferBlock, 0, read); // Copy the file.
      }
      
      fileInputStream.close();
      fileOutputStream.close();
      return hash32.getValue()+""; // Force to be a string so that if we can change to use another hashing algorithm.
      
    }
    catch(UnsupportedEncodingException ex)
    {
      ex.printStackTrace();
    }
    catch(IOException ex)
    {
      ex.printStackTrace();
    }
    
    return null;    
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
}