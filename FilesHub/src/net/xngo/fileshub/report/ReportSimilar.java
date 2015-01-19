package net.xngo.fileshub.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import net.xngo.fileshub.Config;
import net.xngo.fileshub.Utils;
import net.xngo.fileshub.struct.PairFile;
import net.xngo.utils.java.io.FileUtils;

public class ReportSimilar extends ReportGeneric
{
  public ReportSimilar(File file)
  {
    super(file);
  }
  
  public void writePotentialDuplicatesInHtml(List<PairFile> pairFileList)
  {
    StringBuilder divLines = new StringBuilder();
    for(int i=0; i<pairFileList.size(); i++)
    {
      // Add double quote so that it is easier to manipulate on command line.
      String left = super.doubleQuote(pairFileList.get(i).fileA);
      String right= super.doubleQuote(pairFileList.get(i).fileB);
      long fileASize = new File(pairFileList.get(i).fileA).length();
      long fileBSize = new File(pairFileList.get(i).fileB).length();
      
      // Construct the difference with HTML elements.
      Difference difference = new Difference(left, right);
      difference.computeSpan();
      String leftSpan = super.printDelete(difference.getLeftSpan()); // Not elegant.
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
      FileWriter htmlWriter = new FileWriter(super.file);
      BufferedWriter htmlWriterBuffer = new BufferedWriter(htmlWriter);
      htmlWriterBuffer.write(html);
      htmlWriterBuffer.close();
      htmlWriter.close();
      System.out.println(String.format("\nResults are stored in %s.", Utils.getCanonicalPath(super.file)));
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    
  }    
}
