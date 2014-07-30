package net.xngo.fileshub;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import net.xngo.fileshub.db.Database;
import net.xngo.fileshub.db.Repository;
import net.xngo.fileshub.struct.ResultDocSet;


/**
 * 
 * @author Xuan Ngo
 *
 */
public class Hub
{
  private Database database = new Database();
  
  public Hub()
  {
    database.create();
  }
  
  public void addFiles(Set<File> listOfFiles)
  {
    ArrayList<ResultDocSet> listOfDuplicateFiles = new ArrayList<ResultDocSet>();
    
    Repository repository = new Repository();
    long totalSize = 0;
    for (File file : listOfFiles) 
    {
      ResultDocSet resultDocSet = repository.addFile(file);
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
      System.out.println(String.format("%s ==> %s", Utils.getCanonicalPath(listOfDuplicateFiles.get(i).file), listOfDuplicateFiles.get(i).shelfDoc.canonical_path));
    }
    
    Report report = new Report();
    report.writeCSV(listOfDuplicateFiles, "./results.csv");
    report.write(listOfDuplicateFiles);
    
   
  }
}
