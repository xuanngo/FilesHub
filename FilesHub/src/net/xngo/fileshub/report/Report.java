package net.xngo.fileshub.report;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Arrays;
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

import net.xngo.fileshub.AppInfo;
import net.xngo.fileshub.Utils;
import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.struct.Duplicate;
import net.xngo.utils.java.io.FileUtils;

public class Report
{
  public static int  FILES_TO_PROCESS      = 0;
  public static long FILES_SIZE            = 0;
  public static int  DUPLICATE_FILES       = 0;
  public static long DUPLICATE_FILES_SIZE  = 0;
  
  public static String START_TIME   = "";
  public static String END_TIME     = "";
  public static String ELAPSED_TIME = "";
  
  private StringBuilder summary = new StringBuilder();
  
  private ArrayList<Duplicate> duplicates = new ArrayList<Duplicate>();
  private String totalDuplicateSizeString = "";
  
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
      long totalDuplicateSize = 0;
      for(Duplicate dup: this.duplicates)
      {
        File file = new File(dup.toAddDoc.canonical_path);
        totalDuplicateSize += file.length();
        System.out.println(dup.toAddDoc.canonical_path);
      }
      
      System.out.println("========================================================");
      this.setDuplicateSizeString(this.duplicates.size(), totalDuplicateSize);
      System.out.println(this.getDuplicateSizeString());
    }
    else
      System.out.println("There is no duplicate file.");
  }
  
  public void displaySummary()
  {
    this.summary.append("========================================================\n");
    
    this.summary.append(String.format("%d files processed.\n", Report.FILES_TO_PROCESS));
    
    // Start at YYYY-MM-DD HH:MM:SS.mmm
    this.summary.append(String.format("Start at %s\n", Report.START_TIME));
    
    // End at YYYY-MM-DD HH:MM:SS.mmm
    this.summary.append(String.format("End   at %s\n", Report.END_TIME));

    // Ran for HH:MM:SS.mmm (milliseconds)
    this.summary.append(String.format("Ran  for %s\n", Report.ELAPSED_TIME));
    
    System.out.println(this.summary.toString());
    
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
  
  
  public void progressPrint(String s)
  {
    for(int i=0; i<s.length(); i++)
    {
      System.out.print('\b');
    }
    System.out.print(s);
  }
  
  
  public void displayTotalFilesToProcess()
  {
    System.out.println(String.format("Files to process = %,d", Report.FILES_TO_PROCESS));
  }
  
  
  public void writeHtml(String filename)
  {
    Chronometer chrono = new Chronometer();
    
chrono.start();
    StringBuilder divLines = new StringBuilder();
    for(int i=0; i<this.duplicates.size(); i++)
    {
      String left = this.doubleQuote(this.duplicates.get(i).toAddDoc.canonical_path);
      String right= this.doubleQuote(this.duplicates.get(i).shelfDoc.canonical_path);
      
      Difference difference = new Difference(left, right);
      difference.computeSpan();
      String leftSpan = this.printDelete(difference.getLeftSpan()); // Not elegant.
      String rightSpan= difference.getRightSpan();
      if(i%2==0)
        divLines.append(String.format("<div class=\"line-even\">%s<br/>%s</div>\n", leftSpan, rightSpan)); // Add \n so that user can process the HTML output.
      else
        divLines.append(String.format("<div class=\"line-odd\">%s<br/>%s</div>\n", leftSpan, rightSpan));  // Add \n so that user can process the HTML output.
    }
    
chrono.stop("DIV");    

    String html = FileUtils.load(AppInfo.HTML_TEMPLATE_PATH);
    html = html.replace("<!-- @SUMMARY -->", this.summary.toString());
    html = html.replace("<!-- @DIFF -->", divLines);

chrono.stop("REPLACE");
chrono.display();
    
    try
    {
      FileWriter htmlWriter = new FileWriter(filename);
      BufferedWriter htmlWriterBuffer = new BufferedWriter(htmlWriter);
      htmlWriterBuffer.write(html);
      htmlWriterBuffer.close();
      htmlWriter.close();
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    
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
    return String.format("del /q %s", path);
  }
  
  private String printDeleteUnix(String path)
  {
    return String.format("rm -f %s", path);
  }
  
  private String doubleQuote(String s)
  {
    return String.format("\"%s\"", s);
  }
  
  private void setDuplicateSizeString(int numOfFiles, long size)
  {
    this.totalDuplicateSizeString = String.format("Total size of %s duplicate files = %s.", numOfFiles, Utils.readableFileSize(size));
  }
  private String getDuplicateSizeString()
  {
    return this.totalDuplicateSizeString;
  }
  
}
