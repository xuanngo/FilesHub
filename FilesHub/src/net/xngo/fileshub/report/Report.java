package net.xngo.fileshub.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.xngo.fileshub.Config;
import net.xngo.utils.java.io.FileUtils;

abstract class Report
{
  protected File file = null;
  protected StringBuilder htmlSummary = new StringBuilder(255); // The top part.
  protected StringBuilder htmlBody = new StringBuilder(255);    // The bottom and last part.
  
  public Report(File file)
  {
    this.file = file;
  }
  public void addSummary(String htmlSummary)
  {
    this.htmlSummary.append(htmlSummary);
  }
  
  
  
  /****************************************************************************
   * 
   *                             ABSTRACT FUNCTIONS
   * 
   ****************************************************************************/    
  abstract protected void constructSummary();
  //abstract public void write();
  
  /****************************************************************************
   * 
   *                             PROTECTED FUNCTIONS
   * 
   ****************************************************************************/  
  protected void displaySummary()
  {
    System.out.println(this.htmlSummary);
  }
  
  protected String doubleQuote(String s)
  {
    return String.format("\"%s\"", s);
  }
  
  protected String printDelete(String path)
  {
    String os_name = System.getProperty("os.name");
    if(os_name.indexOf("Windows")!=-1)
      return this.printDeleteWin(path);
    else
      return this.printDeleteUnix(path);
  }
  protected void addBody(String htmlBody)
  {
    this.htmlBody.append(htmlBody);
  }

  protected void writeToFile()
  {
    // Load html template file and replace the placehoders.
    String html = FileUtils.load(Config.HTML_TEMPLATE_PATH);
    html = html.replace("<!-- @SUMMARY -->", this.htmlSummary.toString());
    html = html.replace("<!-- @DIFF -->", this.htmlBody.toString());
    
    try
    {
      FileWriter htmlWriter = new FileWriter(this.file);
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
  private String printDeleteWin(String path)
  {
    return String.format("del /q %s", path);
  }
  
  private String printDeleteUnix(String path)
  {
    return String.format("rm -f %s", path);
  }  
}
