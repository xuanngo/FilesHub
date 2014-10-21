package net.xngo.fileshub.report;

public class Console
{
  private int lastLineLength = 0;
  private StringBuilder progressLine = new StringBuilder();
  
  public void printProgress(String line)
  {
    this.progressLine.append('\r');
    this.progressLine.append(line);
    
    // If last line is longer, then mask with space.
    final int spaces = lastLineLength-line.length();
    for(int i=0; i<spaces; i++)
      this.progressLine.append(' ');
    
    // Print the progress line.
    System.out.print(this.progressLine.toString());
    this.progressLine.setLength(0); // Reset the progress line.
    
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
