package net.xngo.fileshub;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.io.FileWriter;
import java.io.IOException;

import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.constraint.NotNull;

import net.xngo.fileshub.db.PairFile;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Report
{
  public void writeCSV(ArrayList<PairFile> listOfDuplicates, String csvFilePath)
  {
    ICsvListWriter listWriter = null;
    try
    {
      listWriter = new CsvListWriter(new FileWriter(csvFilePath),
              CsvPreference.STANDARD_PREFERENCE);
      
      final CellProcessor[] processors = this.getProcessors();
      final String[] header = new String[] { "To Delete", "From Database" };
      
      // write the header
      listWriter.writeHeader(header);
      
      // write
      for(int i=0; i<listOfDuplicates.size(); i++)
      {
        final List<Object> row = Arrays.asList(new Object[] { Utils.getCanonicalPath(listOfDuplicates.get(i).toAddFile), Utils.getCanonicalPath(listOfDuplicates.get(i).dbFile)});            
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
  
  
  public void write(ArrayList<PairFile> listOfDuplicates)
  {

    try
    {
      FileWriter toDeleteFile = new FileWriter("result_to_delete.txt");
      FileWriter fromDatabaseFile = new FileWriter("result_from_database.txt");
      BufferedWriter toDeleteFileBuffer = new BufferedWriter(toDeleteFile);
      BufferedWriter fromDatabaseFileBuffer = new BufferedWriter(fromDatabaseFile);
      
      for(int i=0; i<listOfDuplicates.size(); i++)
      {
        toDeleteFileBuffer.write(Utils.getCanonicalPath(listOfDuplicates.get(i).toAddFile)+"\n");
        fromDatabaseFileBuffer.write(Utils.getCanonicalPath(listOfDuplicates.get(i).dbFile)+"\n");
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
