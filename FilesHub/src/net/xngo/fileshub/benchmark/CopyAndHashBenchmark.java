package net.xngo.fileshub.benchmark;

import java.util.ArrayList;
import java.util.Collection;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import net.xngo.fileshub.Utils;
/**
 * Since this a benchmark, avoid any performance speedups from caching. Use different source files and destination files.
 * @author Xuan Ngo
 *
 */
public class CopyAndHashBenchmark
{
  Utils util = new Utils();

  public static void main(String[] args)
  {
    CopyAndHashBenchmark cpNhashBen = new CopyAndHashBenchmark();
    cpNhashBen.benchmarkCopyNhash();
    System.out.println("Done!");
  }
  
  public void benchmarkCopyNhash()
  {
    File directory = new File("./releases/latest"); // Use latest release files as test data.
    Collection<File> filesList = FileUtils.listFiles(directory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
    
    final int MAX_TRIES = 10;

    for (File file : filesList) 
    {
      String filesize = this.readableFileSize(file.length());
      double average = 0.0;
      
      average = this.copyNhashSeparately(file, MAX_TRIES);
      System.out.println(String.format("%s(%s): Average(%d) = %,17.2f ns => %s", file.getName(), filesize, MAX_TRIES, average, "copyNhashSeparately"));

      average = this.copyNhashCombined(file, MAX_TRIES);
      System.out.println(String.format("%s(%s): Average(%d) = %,17.2f ns => %s", file.getName(), filesize, MAX_TRIES, average, "copyNhashCombined"));
      
      System.out.println();
    }

    //util.copyFileUsingFileChannels(source, dest);
  }
  

  public double copyNhashSeparately(File file, int MAX_TRIES)
  {

    long start;
    long end;
    long total = 0;
    
    for(int i=0; i<MAX_TRIES; i++)
    {
      try
      {
        File dest = File.createTempFile(file.getName(), ".tmp");
        
        // Benchmark start here.
        start = System.nanoTime();
        util.copyFileUsingFileChannels(file, dest);
        util.getHash(dest);
        end = System.nanoTime();
        
        total += end - start;
        
        //System.out.println(i+":"+dest.getAbsolutePath()+" : "+(end - start));
        dest.delete(); // Clean up.
        
      }
      catch(IOException ex)
      {
        System.out.println(ex);
      }
    }
    
    Double dTotal = new Long(total).doubleValue();
    Double dtries = new Integer(MAX_TRIES).doubleValue();
    
    return dTotal/dtries; // Return the average.
    
  }
  
  public double copyNhashCombined(File file, int MAX_TRIES)
  {

    long start;
    long end;
    long total = 0;
    
    for(int i=0; i<MAX_TRIES; i++)
    {
      try
      {
        File dest = File.createTempFile(file.getName(), ".tmp");
        
        // Benchmark start here.
        start = System.nanoTime();
        util.copyNhash(file, dest);
        end = System.nanoTime();
        
        total += end - start;
        
        //System.out.println(i+":"+dest.getAbsolutePath()+" : "+(end - start));
        dest.delete(); // Clean up.
        
      }
      catch(IOException ex)
      {
        System.out.println(ex);
      }
    }
    
    Double dTotal = new Long(total).doubleValue();
    Double dtries = new Integer(MAX_TRIES).doubleValue();
    
    return dTotal/dtries; // Return the average.
    
  }
  
  /**
   * Return human readable file size.
   * This is a help function.
   * Source: http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
   * 
   * @param size
   * @return
   */
  private String readableFileSize(long size) 
  {
    if(size <= 0) return "0";
    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
  }
  
}
