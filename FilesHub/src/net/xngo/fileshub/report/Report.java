package net.xngo.fileshub.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;


import net.xngo.fileshub.Config;
import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.struct.Duplicate;
import net.xngo.fileshub.struct.PairFile;
import net.xngo.utils.java.io.FileUtils;

public class Report
{
  public static String  DIRECTORIES           = "";
  public static int     FILES_TO_PROCESS      = 0;
  public static long    FILES_SIZE            = 0; // in bytes.
  public static int     DUPLICATE_FILES       = 0;
  public static long    DUPLICATE_FILES_SIZE  = 0; // in bytes.
  
  public static String START_TIME   = "";
  public static String END_TIME     = "";
  public static String ELAPSED_TIME = "";
  
  private StringBuilder summary = new StringBuilder();
  
  private ArrayList<Duplicate> duplicates = new ArrayList<Duplicate>();
  
  public Console console = new Console();
  
  public void addDuplicate(Document toAddDoc, Document shelfDoc)
  {
    this.duplicates.add(new Duplicate(toAddDoc, shelfDoc));
    DUPLICATE_FILES_SIZE += new File(toAddDoc.canonical_path).length();
  }
  
  public void sort()
  {
    // Sort
    Collections.sort(this.duplicates);
  }
  
  public void constructSummary()
  {
    /** Gather info. **/
    /*******************************/
    DUPLICATE_FILES = this.duplicates.size();
    
    /** Construct summary details **/
    /*******************************/
    this.summary.append("Summary:\n");
    
    if(!Report.DIRECTORIES.isEmpty()){ this.summary.append(String.format("\tProcessed directories: %s.\n", Report.DIRECTORIES)); }
    
    this.summary.append(String.format("\t%,d files processed.\n", Report.FILES_TO_PROCESS));
    this.summary.append(String.format("\t%,d duplicate file(s) found totalling %s.\n", DUPLICATE_FILES, FileUtils.readableSize(DUPLICATE_FILES_SIZE)));
    
    // Start at YYYY-MM-DD HH:MM:SS.mmm
    this.summary.append(String.format("\tStart at %s\n", Report.START_TIME));
    
    // End at YYYY-MM-DD HH:MM:SS.mmm
    this.summary.append(String.format("\tEnd   at %s\n", Report.END_TIME));

    // Ran for HH:MM:SS.mmm (milliseconds)
    this.summary.append(String.format("\tRan  for %s\n", Report.ELAPSED_TIME));

    // Ran for HH:MM:SS.mmm (milliseconds)
    this.summary.append(String.format("\t%s\n", this.getRAMUsage()));
    
  }
  
  public void displaySummary()
  {
    System.out.println("========================================================");
    System.out.println(this.summary.toString());
  }
  
  
  
  public void displayTotalFilesToProcess()
  {
    System.out.println(String.format("File(s) to process = %,d", Report.FILES_TO_PROCESS));
  }
  
  
  public void writeHtml(String filename)
  {
    StringBuilder divLines = new StringBuilder();
    for(int i=0; i<this.duplicates.size(); i++)
    {
      // Add double quote so that it is easier to manipulate on command line.
      String left = this.doubleQuote(this.duplicates.get(i).toAddDoc.canonical_path);
      String right= this.doubleQuote(this.duplicates.get(i).shelfDoc.canonical_path);
      long toAddSize = new File(this.duplicates.get(i).toAddDoc.canonical_path).length();
      
      // Construct the difference with HTML elements.
      Difference difference = new Difference(left, right);
      difference.computeSpan();
      String leftSpan = this.printDelete(difference.getLeftSpan()); // Not elegant.
      String rightSpan= difference.getRightSpan();
      String sizeSpan = String.format("<span class=\"size\">%s</span>", FileUtils.readableSize(toAddSize));
      if(i%2==0)
        divLines.append(String.format("<div class=\"line-even\">%s<br/>%s %s</div>\n", leftSpan, rightSpan, sizeSpan)); // Add \n so that user can process the HTML output.
      else
        divLines.append(String.format("<div class=\"line-odd\">%s<br/>%s %s</div>\n", leftSpan, rightSpan, sizeSpan));  // Add \n so that user can process the HTML output.
    }

    String html = FileUtils.load(Config.HTML_TEMPLATE_PATH);
    html = html.replace("<!-- @SUMMARY -->", this.summary.toString());
    html = html.replace("<!-- @DIFF -->", divLines);
    
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
  
  public void writePotentialDuplicatesInHtml(String filepath, List<PairFile> pairFileList)
  {
    StringBuilder divLines = new StringBuilder();
    for(int i=0; i<pairFileList.size(); i++)
    {
      // Add double quote so that it is easier to manipulate on command line.
      String left = this.doubleQuote(pairFileList.get(i).fileA);
      String right= this.doubleQuote(pairFileList.get(i).fileB);
      long fileASize = new File(pairFileList.get(i).fileA).length();
      long fileBSize = new File(pairFileList.get(i).fileB).length();
      
      // Construct the difference with HTML elements.
      Difference difference = new Difference(left, right);
      difference.computeSpan();
      String leftSpan = this.printDelete(difference.getLeftSpan()); // Not elegant.
      String rightSpan= difference.getRightSpan();
      String fileASizeSpan = String.format("<span class=\"size\">%s</span>", FileUtils.readableSize(fileASize));
      String fileBSizeSpan = String.format("<span class=\"size\">%s</span>", FileUtils.readableSize(fileBSize));
      if(i%2==0)
        divLines.append(String.format("<div class=\"line-even\">[%3d%%] %s %s<br/>%s %s</div>\n", pairFileList.get(i).similarRate, leftSpan, fileASizeSpan, rightSpan, fileBSizeSpan)); // Add \n so that user can process the HTML output.
      else
        divLines.append(String.format("<div class=\"line-odd\">[%3d%%] %s %s<br/>%s %s</div>\n", pairFileList.get(i).similarRate, leftSpan, fileASizeSpan, rightSpan, fileBSizeSpan));  // Add \n so that user can process the HTML output.
    }

    String html = FileUtils.load(Config.HTML_TEMPLATE_PATH);
    html = html.replace("<!-- @SUMMARY -->", this.summary.toString());
    html = html.replace("<!-- @DIFF -->", divLines);
    
    try
    {
      FileWriter htmlWriter = new FileWriter(filepath);
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
  
  public String getRAMUsage()
  {
    Runtime runtime = Runtime.getRuntime();
    long usedMemory = runtime.totalMemory()-runtime.freeMemory();
    return String.format("RAM: %s / %s Max=%s", FileUtils.readableSize(usedMemory           ), 
                                                FileUtils.readableSize(runtime.totalMemory()), 
                                                FileUtils.readableSize(runtime.maxMemory()) );
  }
  
}
