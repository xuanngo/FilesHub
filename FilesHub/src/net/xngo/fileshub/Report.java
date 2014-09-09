package net.xngo.fileshub;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;

import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.constraint.NotNull;

import net.xngo.fileshub.struct.Document;

public class Report
{
  private ArrayList<Document> toAddDocs = new ArrayList<Document>();
  private ArrayList<Document> existingDocs = new ArrayList<Document>();
  
  public void addDuplicate(Document toAddDoc, Document existingDoc)
  {
    this.toAddDocs.add(toAddDoc);
    this.existingDocs.add(existingDoc);
  }
  
  public void display()
  {
    if(this.toAddDocs.size()>0)
    {
      System.out.println("Duplicate files:");
      System.out.println("=================");
      
      // Get total size.
      long totalSize = 0;
      for(Document toAddDoc: this.toAddDocs)
      {
        File file = new File(toAddDoc.canonical_path);
        totalSize += file.length();
        System.out.println(toAddDoc.canonical_path);
      }
      
      System.out.println("========================================================");
      System.out.println(String.format("Total size of %s duplicate files = %s.", toAddDocs.size(), Utils.readableFileSize(totalSize)));
    }
    else
      System.out.println("There is no duplicate file.");
  }
  public void writeCSV(String csvFilePath)
  {
    ICsvListWriter listWriter = null;
    try
    {
      listWriter = new CsvListWriter(new FileWriter(csvFilePath),
              CsvPreference.STANDARD_PREFERENCE);
      
      final CellProcessor[] processors = this.getProcessors();
      final String[] header = new String[] { "Duplicate", "From Database" };
      
      // write the header
      listWriter.writeHeader(header);
      
      // write
      for(int i=0; i<toAddDocs.size(); i++)
      {
        final List<Object> row = Arrays.asList(new Object[] { this.toAddDocs.get(i).canonical_path, this.existingDocs.get(i).canonical_path});            
        listWriter.write(row, processors);
      }
            
      if( listWriter != null )
      {
        listWriter.close();
      }            
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
  
  }
  
  private CellProcessor[] getProcessors()
  {
    
    final CellProcessor[] processors = new CellProcessor[] { 
                                                              new NotNull(), // Duplicate file.
                                                              new NotNull(), // File in database
                                                            };
    
    return processors;
  }
  
  
  public void write()
  {
    try
    {
      FileWriter toDeleteFile = new FileWriter("result_to_delete.txt");
      FileWriter toDeleteExecutable = new FileWriter("result_to_delete_executable.txt");
      FileWriter fromDatabaseFile = new FileWriter("result_from_database.txt");
      BufferedWriter toDeleteFileBuffer = new BufferedWriter(toDeleteFile);
      BufferedWriter toDeleteFileExecutableBuffer = new BufferedWriter(toDeleteExecutable);
      BufferedWriter fromDatabaseFileBuffer = new BufferedWriter(fromDatabaseFile);
      
      for(int i=0; i<this.toAddDocs.size(); i++)
      {
        toDeleteFileExecutableBuffer.write(this.printDelete(this.toAddDocs.get(i).canonical_path)+"\n");
        toDeleteFileBuffer.write(this.toAddDocs.get(i).canonical_path+"\n");
        fromDatabaseFileBuffer.write(this.existingDocs.get(i).canonical_path+"\n");
      } 
      
      toDeleteFileBuffer.close();
      fromDatabaseFileBuffer.close();
      toDeleteFileExecutableBuffer.close();
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    
  }

  public void progressPrint(String s)
  {
    for(int i=0; i<s.length(); i++)
    {
      System.out.print('\b');
    }
    System.out.print(s);
  }
  
  
  public void displayTotalFiles(int totalFiles)
  {
    System.out.println(String.format("Files to process = %,d", totalFiles));
  }
  
  /****************************************************************************
   * 
   *                             PRIVATE FUNCTIONS
   * 
   ****************************************************************************/
  
  private String printDelete(String path)
  {
    String os_name = System.getProperty("os.name");
    if(os_name.indexOf("Windows")!=-1)
      return this.printDeleteWin(path);
    else
      return this.printDeleteUnix(path);
    
  }
  private String printDeleteWin(String path)
  {
    return String.format("del /q \"%s\"", path);
  }
  
  private String printDeleteUnix(String path)
  {
    return String.format("rm -f \"%s\"", path);
  }
  
}
