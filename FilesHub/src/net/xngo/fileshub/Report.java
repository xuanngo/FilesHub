package net.xngo.fileshub;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Collections;
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
import net.xngo.fileshub.struct.Duplicate;

public class Report
{
  
  private ArrayList<Duplicate> duplicates = new ArrayList<Duplicate>();
  
  
  public void addDuplicate(Document toAddDoc, Document shelfDoc)
  {
    this.duplicates.add(new Duplicate(toAddDoc, shelfDoc));
  }
  
  public void sort()
  {
    // Sort
    Collections.sort(this.duplicates);
  }
  
  public void display()
  {
    if(this.duplicates.size()>0)
    {

      System.out.println("Duplicate files:");
      System.out.println("=================");
      
      // Get total size.
      long totalSize = 0;
      for(Duplicate dup: this.duplicates)
      {
        File file = new File(dup.toAddDoc.canonical_path);
        totalSize += file.length();
        System.out.println(dup.toAddDoc.canonical_path);
      }
      
      System.out.println("========================================================");
      System.out.println(String.format("Total size of %s duplicate files = %s.", this.duplicates.size(), Utils.readableFileSize(totalSize)));
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
      for(int i=0; i<this.duplicates.size(); i++)
      {
        final List<Object> row = Arrays.asList(new Object[] { this.duplicates.get(i).toAddDoc.canonical_path, this.duplicates.get(i).shelfDoc.canonical_path});            
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
      
      for(int i=0; i<this.duplicates.size(); i++)
      {
        toDeleteFileExecutableBuffer.write(this.printDelete(this.doubleQuote(this.duplicates.get(i).toAddDoc.canonical_path))+"\n");
        toDeleteFileBuffer.write(this.doubleQuote(this.duplicates.get(i).toAddDoc.canonical_path)+"\n");
        fromDatabaseFileBuffer.write(this.doubleQuote(this.duplicates.get(i).shelfDoc.canonical_path)+"\n");
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
  
  private String doubleQuote(String s)
  {
    return String.format("\"%s\"", s);
  }
}
