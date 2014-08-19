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

import net.xngo.fileshub.struct.ResultDocSet;

public class Report
{
  private ArrayList<String> toAddPaths = new ArrayList<String>();
  private ArrayList<String> existingPaths = new ArrayList<String>();
  
  public void addDuplicate(String toAddPath, String existingPath)
  {
    this.toAddPaths.add(toAddPath);
    this.existingPaths.add(existingPath);
  }
  public void display()
  {
    if(toAddPaths.size()>0)
    {
      System.out.println("Duplicate files:");
      System.out.println("=================");
      
      // Get total size.
      long totalSize = 0;
      for(String toAddPath: toAddPaths)
      {
        File file = new File(toAddPath);
        totalSize += file.length();
        System.out.println(toAddPath);
      }
      
      System.out.println(String.format("Total size of duplicate files = %s.", Utils.readableFileSize(totalSize)));
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
      for(int i=0; i<toAddPaths.size(); i++)
      {
        final List<Object> row = Arrays.asList(new Object[] { this.toAddPaths.get(i), this.existingPaths.get(i)});            
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
  
  
  public void write(ArrayList<ResultDocSet> listOfDuplicates)
  {

    try
    {
      FileWriter toDeleteFile = new FileWriter("result_to_delete.txt");
      FileWriter fromDatabaseFile = new FileWriter("result_from_database.txt");
      BufferedWriter toDeleteFileBuffer = new BufferedWriter(toDeleteFile);
      BufferedWriter fromDatabaseFileBuffer = new BufferedWriter(fromDatabaseFile);
      
      for(int i=0; i<listOfDuplicates.size(); i++)
      {
        toDeleteFileBuffer.write(Utils.getCanonicalPath(listOfDuplicates.get(i).file)+"\n");
        fromDatabaseFileBuffer.write(listOfDuplicates.get(i).document.canonical_path+"\n");
      } 
      
      toDeleteFileBuffer.close();
      fromDatabaseFileBuffer.close();
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }     
    
 
  }

}
