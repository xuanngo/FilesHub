package net.xngo.fileshub.benchmark;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import net.xngo.fileshub.Utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
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
  }
  
  public void benchmarkCopyNhash()
  {
    File directory = new File("./releases/latest"); // Use latest release files as test data.
    Collection<File> filesList = FileUtils.listFiles(directory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
    
    final int MAX_TRIES = 10;

    File benchmarkFile = new File("./test/benchmark.csv");
    if(!benchmarkFile.exists())
    {
      String data = String.format("Filename, size, System.currentTimeMillis(), Avg. copyNhashSeparately() in ns(Tries=%d), Avg. copyNhashCombined() in ns(Tries=%d)\n", MAX_TRIES, MAX_TRIES);
      try
      {
        FileUtils.writeStringToFile(benchmarkFile, data);
      }
      catch(IOException e){ e.printStackTrace(); }
    }
    
    long currentTimeMillis = System.currentTimeMillis();
    for (File file : filesList) 
    {
      String filesize = net.xngo.utils.java.io.FileUtils.readableSize(file.length());
      
      double averageCopyNhashSeparately = this.copyNhashSeparately(file, MAX_TRIES);
      double averageCopyNhashCombined = this.copyNhashCombined(file, MAX_TRIES);
      
      String data = String.format("%s, %s, %d, %.2f, %.2f\n", file.getName(), filesize, currentTimeMillis, averageCopyNhashSeparately, averageCopyNhashCombined);
      try
      {
        FileUtils.writeStringToFile(benchmarkFile, data, true);
      }
      catch(IOException e){ e.printStackTrace(); }      
    }
    System.out.println(new java.util.Date()+": Benchmark done!");
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
        Utils.getHash(dest);
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
  

  
}
