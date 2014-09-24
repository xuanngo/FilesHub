package net.xngo.fileshub;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import net.xngo.fileshub.db.Debug;
import net.xngo.fileshub.db.Manager;
import net.xngo.fileshub.report.Report;
import net.xngo.fileshub.struct.Document;
import net.xngo.utils.java.time.ElapsedTime;
import net.xngo.utils.java.io.FileUtils;
import net.xngo.utils.java.math.Math;


/**
 * 
 * @author Xuan Ngo
 *
 */
public class Hub
{
  private Manager manager = new Manager();
  
  public Hub()
  {
    this.manager.createDbStructure();
  }
  
  public void addFiles(Set<File> listOfFiles, List<File> addPaths)
  {
    Config.chrono.stop("Get all files to process");
    
    // Display total number of files to process.
    Report report = new Report();
    Report.FILES_TO_PROCESS = listOfFiles.size();
    report.displayTotalFilesToProcess();
      
    
    // Preparation to display the progress.
    Report.FILES_SIZE = FileUtils.totalSize(listOfFiles);
    String totalReadableSize = FileUtils.readableSize(Report.FILES_SIZE);
    long size = 0;
    int whenToDisplay = 11;
    
    Config.chrono.stop("Get total file size");
    int i=1; // 1 because progress % is print after some files are processed.
    for (File file : listOfFiles) 
    {
      Debug.msg(String.format("Adding [%s]", file.getAbsolutePath()));
      
      try
      {
        // Add file to database.
        Document doc = this.manager.addFile(file);
        
        // Collect duplicate entries for report.
        if(doc!=null)
        {
          if(doc.canonical_path.compareTo(Utils.getCanonicalPath(file))!=0) // Ignore if users add the exact same file and the same path.
            report.addDuplicate(new Document(file), doc);
        }
      }
      catch(RuntimeException e)
      {
        if(e.getMessage().indexOf("The process cannot access the file because another process has locked a portion of the file")!=-1)
          System.out.println(String.format("Warning: Ignore locked file(%s).", file.getAbsolutePath()));
        else
        {
          RuntimeException rException = new RuntimeException(e.getMessage());
          rException.setStackTrace(e.getStackTrace());
          throw rException;
        }
        
      }
      
      // Print progress to console.      
      size += file.length();
      i++;
      if( (i%whenToDisplay)==0)
      {
        report.console.printProgress(String.format("%s [%s] [%d/%d] %s", Math.getReadablePercentage(size, Report.FILES_SIZE), 
                                                                          totalReadableSize, 
                                                                          i, 
                                                                          Report.FILES_TO_PROCESS,
                                                                          report.getRAMUsage()));
      }
      
    }
    report.console.printProgress(String.format("100.00%% [%s] [%d/%d]", totalReadableSize, Report.FILES_TO_PROCESS, Report.FILES_TO_PROCESS));// Last display because of the remainder of modulus.
    System.out.println();
    Config.chrono.stop("Add files");
    
    Report.START_TIME   = Config.chrono.getStartTime();
    Report.END_TIME     = Config.chrono.getEndTime();
    Report.ELAPSED_TIME = Config.chrono.getTotalRuntimeString();
    
    report.sort();
    Config.chrono.stop("Sort duplicates");
    report.displayDuplicates();
    Config.chrono.stop("Display duplicates");
    report.constructSummary();    
    report.writeCSV(String.format("./results_%s.csv", this.getResultsSuffix(addPaths)));   // Use ./XYZ so it writes results to the executed location.
    Config.chrono.stop("Write CSV file");
    report.writeHtml(String.format("./results_%s.html", this.getResultsSuffix(addPaths)));
    Config.chrono.stop("Write HTML file");
    report.displaySummary();
    Config.chrono.display();
    
  }
  
  public void update()
  {
    List<Document> missingFileList = this.manager.update();
    
    if(missingFileList.size()>0)
    {
      System.out.println(String.format("\nMissing files not in your system:", missingFileList.size()));
      System.out.println("===================================");
      for(Document doc: missingFileList)
      {
        System.out.println("\t"+doc.canonical_path);
      }
      System.out.println(String.format("%d files are missing from your system!", missingFileList.size()));
    }
  }
  
  public void markDuplicate(File duplicate, File of)
  {
    this.manager.markDuplicate(duplicate, of);
  }
  
  public void hash(Set<File> files)
  {
    ArrayList<File> sortedFiles = new ArrayList<File>(files);
    
    Collections.sort(sortedFiles);
    for(File file: sortedFiles)
    {
      String hash = Utils.getHash(file);
      try
      {
        System.out.println(String.format("%12s %s", hash, file.getCanonicalPath()));
      }
      catch(IOException ex)
      {
        ex.printStackTrace();
      }
    }
  }
  
  /****************************************************************************
   * 
   *                             PRIVATE FUNCTIONS
   * 
   ****************************************************************************/  
  
  private String getResultsSuffix(List<File> addPaths)
  {
    StringBuilder directories = new StringBuilder();
    for(File path: addPaths)
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
            directories.append("_");
          }
        }
      }
    }
    
    String timestamp = System.currentTimeMillis()+"";
    
    if(directories.toString().isEmpty())
      return timestamp;
    else
      return directories.toString()+timestamp;

  }
}
