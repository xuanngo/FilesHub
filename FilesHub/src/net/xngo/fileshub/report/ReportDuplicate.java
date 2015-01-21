package net.xngo.fileshub.report;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import net.xngo.fileshub.Main;
import net.xngo.fileshub.Utils;
import net.xngo.fileshub.struct.Document;
import net.xngo.fileshub.struct.Duplicate;
import net.xngo.utils.java.io.FileUtils;

public class ReportDuplicate extends Report
{
  private ArrayList<Duplicate> duplicates = new ArrayList<Duplicate>();
  
  private long totalDuplicateSize = 0;
  private int numberOfDuplicates = 0;
  
  private int totalFilesToProcess = 0;
  
  private String directories = "";
  
  public ReportDuplicate(File file)
  {
    super(file);
  }
  
  public void generate()
  {
    // Sort
    Collections.sort(this.duplicates);
    Main.chrono.stop("Sort duplicates");
    
    // Generate html file.
    this.constructSummary();
    super.addBody(this.generateBody());
    super.writeToFile();
    Main.chrono.stop("Write HTML file");
    
    // Display summary in console.
    super.displaySummary();    
  }
  
  public void addTotalFilesToProcess(int totalNumberOfFiles)
  {
    totalFilesToProcess = totalNumberOfFiles;
  }

  public void addDirectoriesProcessed(String directories)
  {
    this.directories = directories;
  }
  
  public void addDuplicate(Document toAddDoc, Document shelfDoc)
  {
    this.duplicates.add(new Duplicate(toAddDoc, shelfDoc));
    totalDuplicateSize += new File(toAddDoc.canonical_path).length();
    numberOfDuplicates++;
  }
  private String generateBody()
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
      difference.compute();
      String leftSpan = this.printDelete(difference.getLeftSpan()); // Not elegant.
      String rightSpan= difference.getRightSpan();
      String sizeSpan = String.format("<span class=\"size\">%s</span>", FileUtils.readableSize(toAddSize));
      if(i%2==0)
        divLines.append(String.format("<div class=\"line-even\">%s<br/>%s %s</div>\n", leftSpan, rightSpan, sizeSpan)); // Add \n so that user can process the HTML output.
      else
        divLines.append(String.format("<div class=\"line-odd\">%s<br/>%s %s</div>\n", leftSpan, rightSpan, sizeSpan));  // Add \n so that user can process the HTML output.
    }
    
    return divLines.toString();
  }

  protected void constructSummary()
  {
    
    /** Construct summary details **/
    /*******************************/
    super.addSummary("Summary:\n");
    
    if(!this.directories.isEmpty()){ super.addSummary(String.format("\tProcessed directories: %s.\n", this.directories)); }
    
    super.addSummary(String.format("\t%,d files processed.\n", this.totalFilesToProcess));
    super.addSummary(String.format("\t%,d duplicate file(s) found totalling %s.\n", this.numberOfDuplicates, FileUtils.readableSize(this.totalDuplicateSize)));
    
    // Start at YYYY-MM-DD HH:MM:SS.mmm
    super.addSummary(String.format("\tStart at %s\n", Main.chrono.getStartTime()));
    
    // End at YYYY-MM-DD HH:MM:SS.mmm
    super.addSummary(String.format("\tEnd   at %s\n", Main.chrono.getEndTime()));

    // Ran for HH:MM:SS.mmm (milliseconds)
    super.addSummary(String.format("\tRan  for %s\n", Main.chrono.getTotalRuntimeString()));

    // Display memory usage.
    super.addSummary(String.format("\t%s\n", Utils.getRAMUsage()));
    
  }
  
}
