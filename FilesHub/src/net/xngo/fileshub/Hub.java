package net.xngo.fileshub;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
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
  
  public void addFiles(Set<File> listOfFiles)
  {
    // Display total number of files to process.
    Report report = new Report();
    report.displayTotalFiles(listOfFiles.size());
      
    
    // Preparation to display the progress.
    long totalSize = FileUtils.totalSize(listOfFiles);
    String totalReadableSize = FileUtils.readableSize(totalSize);
    long size = 0;
    long totalFiles = listOfFiles.size();
    int whenToDisplay = 5;
    
    ElapsedTime elapsedTime = new ElapsedTime();
    elapsedTime.start();
    int i=1;
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
        report.progressPrint(String.format("%s [%s] [%d/%d]", Math.getReadablePercentage(size, totalSize), totalReadableSize, i, totalFiles));
      }
      
    }
    report.progressPrint(String.format("100.00%% [%s] [%d/%d]", totalReadableSize, totalFiles, totalFiles));// Last display because of the remainder of modulus.
    System.out.println();
    elapsedTime.stop();
    
    report.sort();
    report.display();
    report.writeCSV("./results.csv");
    report.writeHtml("./results.html");
    
    System.out.println("\n===============================================");
    elapsedTime.display();
    System.out.println("===============================================");    
  }
  
  public void update()
  {
    List<Document> missingFileList = this.manager.update();
    
    if(missingFileList.size()>0)
    {
      System.out.println(String.format("Missing files not in your system:", missingFileList.size()));
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
  
}
