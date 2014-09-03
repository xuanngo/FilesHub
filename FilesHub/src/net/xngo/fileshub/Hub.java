package net.xngo.fileshub;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;



import net.xngo.fileshub.db.Debug;
import net.xngo.fileshub.db.Manager;
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
    System.out.println(String.format("Total number of files to process = %d", listOfFiles.size()));
    
    // Preparation to display the progress.
    long totalSize = FileUtils.getTotalSize(listOfFiles);
    String totalReadableSize = FileUtils.readableFileSize(totalSize);
    long size = 0;
    long totalFiles = listOfFiles.size();
    int whenToDisplay = 5;
    
    Report report = new Report();
    ElapsedTime elapsedTime = new ElapsedTime();
    
    elapsedTime.start();
    int i=1;
    for (File file : listOfFiles) 
    {
      Debug.msg(String.format("Adding [%s]", file.getAbsolutePath()));
      
      if(!Utils.isFileLocked(file))
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
      else
        System.out.println(String.format("Warning: Ignore locked file [%s].", file.getAbsolutePath()));
      
      
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
    
    report.display();
    report.writeCSV("./results.csv");
    report.write();
    
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
  
}
