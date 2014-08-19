package net.xngo.fileshub;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


import net.xngo.fileshub.db.Manager;
import net.xngo.fileshub.struct.ResultDocSet;
import net.xngo.fileshub.struct.Document;


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
    for (File file : listOfFiles) 
    {
      Document doc = this.manager.addFile(file);
      if(doc!=null)
      {
        if(doc.canonical_path.compareTo(Utils.getCanonicalPath(file))!=0) // Ignore if users add the exact same file and the same path.
          report.addDuplicate(Utils.getCanonicalPath(file), doc.canonical_path);
      }
    }
    
    report.display();
    report.writeCSV("./results.csv");
    report.write();
  }
  /*
  public void addFiles_old(Set<File> listOfFiles)
  {
    ArrayList<ResultDocSet> listOfDuplicateFiles = new ArrayList<ResultDocSet>();
    
    long totalSize = 0;
    for (File file : listOfFiles) 
    {
      ResultDocSet resultDocSet = this.manager.addFile(file);
      if(resultDocSet.status==ResultDocSet.DIFF_PATH_SAME_HASH)
      {
        listOfDuplicateFiles.add(resultDocSet);
        totalSize += file.length();
      }
      
    }
    
    System.out.println();
    System.out.println(String.format("%s duplicates files [%s]:", listOfDuplicateFiles.size(), Utils.readableFileSize(totalSize)));
    System.out.println("==============================");
    System.out.println("To delete ==> From database");
    for(int i=0; i<listOfDuplicateFiles.size(); i++)
    {
      System.out.println(String.format("%s ==> %s", Utils.getCanonicalPath(listOfDuplicateFiles.get(i).file), listOfDuplicateFiles.get(i).document.canonical_path));
    }
    
    Report report = new Report();
    report.writeCSV(listOfDuplicateFiles, "./results.csv");
    report.write(listOfDuplicateFiles);
    
  }
  */
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
  
  
}
