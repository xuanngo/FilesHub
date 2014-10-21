package net.xngo.fileshub.report;

public class Console
{
  private int lastLineLength = 0;
  
  public void printProgress(String line)
  {
    // Print to console.
    System.out.print('\r');
    System.out.print(line);
    
    // If last line is longer, then mask with space.
    final int spaces = lastLineLength-line.length();
    for(int i=0; i<spaces; i++)
      System.out.print(' ');
    
    // Update previous line length.
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
