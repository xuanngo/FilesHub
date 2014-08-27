package net.xngo.fileshub;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


import net.xngo.fileshub.db.Manager;
import net.xngo.fileshub.struct.Document;
import net.xngo.utils.java.time.ElapsedTime;


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
    Report report = new Report();
    int i=1;
    ElapsedTime elapsedTime = new ElapsedTime();
    
    elapsedTime.start();
    int numOfFiles = listOfFiles.size();
    int whenToDisplay = 5;
    for (File file : listOfFiles) 
    {
      // Print progress to console.
      if( (i%whenToDisplay)==0)
        report.progressPrint(String.format("[%d/%d]", i, numOfFiles));
      
      // Add file to database.
      Document doc = this.manager.addFile(file);
      
      // Collect duplicate entries for report.
      if(doc!=null)
      {
        if(doc.canonical_path.compareTo(Utils.getCanonicalPath(file))!=0) // Ignore if users add the exact same file and the same path.
          report.addDuplicate(new Document(file), doc);
      }
      
      i++;
    }
    elapsedTime.stop();
    
    System.out.println("\n===============================================");
    elapsedTime.display();
    System.out.println("===============================================");
    
    report.display();
    report.writeCSV("./results.csv");
    report.write();
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
