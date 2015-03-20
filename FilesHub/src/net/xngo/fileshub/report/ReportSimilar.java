package net.xngo.fileshub.report;

import java.io.File;
import java.util.List;

import net.xngo.fileshub.Main;
import net.xngo.fileshub.Utils;
import net.xngo.fileshub.struct.PairFile;
import net.xngo.utils.java.io.FileUtils;

public class ReportSimilar extends Report
{
  private String combinationsInfo = "";
  private List<PairFile> pairFileList;
  
  public ReportSimilar(File file)
  {
    super(file);
  }
  public void setData(List<PairFile> pairFileList)
  {
    this.pairFileList = pairFileList;
  }
  public void generate()
  {

    this.constructSummary();
    super.addBody(this.generateBody());
    super.writeToFile();
    Main.chrono.stop("Write HTML file");
    System.out.println(String.format("\nResults are stored in %s.", Utils.getCanonicalPath(super.file)));
    super.displaySummary();
  }
  
  public void addCombinationsInfo(String combinationsInfo)
  {
    this.combinationsInfo = combinationsInfo;
  }
  
  private String generateBody()
  {
    StringBuilder divLines = new StringBuilder();
    for(int i=0; i<this.pairFileList.size(); i++)
    {
      // Add double quote so that it is easier to manipulate on command line.
      String left = super.doubleQuote(this.pairFileList.get(i).fileA);
      String right= super.doubleQuote(this.pairFileList.get(i).fileB);
      long fileASize = new File(this.pairFileList.get(i).fileA).length();
      long fileBSize = new File(this.pairFileList.get(i).fileB).length();
      
      // Construct the difference with HTML elements.
      Difference difference = new Difference(left, right);
      difference.compute();
      String leftSpan = super.printDelete(difference.getLeftSpan()); // Not elegant.
      String rightSpan= difference.getRightSpan();
      String fileASizeSpan = String.format("<span class=\"size\">%s</span>", FileUtils.readableSize(fileASize));
      String fileBSizeSpan = String.format("<span class=\"size\">%s</span>", FileUtils.readableSize(fileBSize));
      if(i%2==0)
        divLines.append(String.format("<div class=\"line-even\">[%3d%%] %s %s<br/>%s %s</div>\n", this.pairFileList.get(i).similarRate, leftSpan, fileASizeSpan, rightSpan, fileBSizeSpan)); // Add \n so that user can process the HTML output.
      else
        divLines.append(String.format("<div class=\"line-odd\">[%3d%%] %s %s<br/>%s %s</div>\n", this.pairFileList.get(i).similarRate, leftSpan, fileASizeSpan, rightSpan, fileBSizeSpan));  // Add \n so that user can process the HTML output.
    }
    
    return divLines.toString();
  }
  
  protected void constructSummary()
  {
    /** Construct summary details **/
    /*******************************/
    super.addSummary("Summary:\n");
    
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
