package net.xngo.fileshub.report;

public class Console
{
  private int lastLineLength = 0;
  
  public void printProgress(String dline)
  {
    String line = String.format("[%d] %s", this.lastLineLength, dline); // For debugging purposes.
    
    for(int i=0; i<lastLineLength; i++)
    {
      System.out.print('\b');
    }
    System.out.print(line); 
    this.lastLineLength = line.length();
  }
  
  public void println(String s)
  {
    System.out.println(s);
  }
  
  public void printError(String s)
  {
    System.err.println(s);
  }
  
}
