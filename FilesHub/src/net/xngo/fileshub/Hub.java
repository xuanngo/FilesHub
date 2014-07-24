package net.xngo.fileshub;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import net.xngo.fileshub.db.Database;
import net.xngo.fileshub.db.Document;
import net.xngo.fileshub.db.PairFile;

/**
 * 
 * @author Xuan Ngo
 *
 */
public class Hub
{
  private Database database = new Database();
  
  public Hub(String[] args)
  {
    database.create();
    
    this.addDirectory(new File(args[0]));
  }
  
  public void addDirectory(File directory)
  {
    ArrayList<PairFile> listOfDuplicateFiles = new ArrayList<PairFile>();
    
    Collection<File> filesList = FileUtils.listFiles(directory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
    Document doc = new Document();
    long totalSize = 0;
    for (File file : filesList) 
    {
      PairFile pairFile = doc.addFile(file);
      if(pairFile.uid==PairFile.DUPLICATE_HASH)
      {
        listOfDuplicateFiles.add(pairFile);
        totalSize += pairFile.toAddFile.length();
      }
    }
    
    System.out.println();
    System.out.println(String.format("%s duplicates files [%s]:", listOfDuplicateFiles.size(), Utils.readableFileSize(totalSize)));
    System.out.println("==============================");
    for(int i=0; i<listOfDuplicateFiles.size(); i++)
    {
      String toAddFilePath = Utils.getCanonicalPath(listOfDuplicateFiles.get(i).toAddFile);
      String dbFilePath = Utils.getCanonicalPath(listOfDuplicateFiles.get(i).dbFile);
      System.out.println(String.format("%s ==> %s", toAddFilePath, dbFilePath));
    }
    
    Report report = new Report();
    report.writeCSV(listOfDuplicateFiles, "./resultCSV.csv");
  }
}
