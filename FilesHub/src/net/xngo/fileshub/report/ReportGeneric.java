package net.xngo.fileshub.report;

import java.io.File;

public class ReportGeneric
{
  public StringBuilder summary = new StringBuilder();
  protected File file = null;
  
  public ReportGeneric(File file)
  {
    this.file = file;
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
