package net.xngo.fileshub.report;

import java.util.LinkedList;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Operation;

public class Difference
{
  
  private LinkedList<Diff> deltas = null;
  private String leftSpan="";
  private String rightSpan="";
  
  private int similarRate = 0;

  public Difference(String left, String right)
  {
    diff_match_patch diffMatchPatch = new diff_match_patch();
    this.deltas = diffMatchPatch.diff_main(left, right);
    diffMatchPatch.diff_cleanupSemantic(this.deltas);
    
    
    // Calculate the similar rate.
    int levenshtein = diffMatchPatch.diff_levenshtein(this.deltas);
    int maxLength = 0;
    if(left.length()>right.length())
      maxLength = left.length();
    else
      maxLength = right.length();

//System.out.println(String.format("\tl=%d, m=%d", levenshtein, maxLength));
    this.similarRate = (int)Math.ceil((double)(maxLength-levenshtein)/(double)maxLength*100.0);
  }
  
  public int getSimilarRate()
  {
    return this.similarRate;
  }
  
  public void computeSpan()
  {
    this.leftSpan = "<span class=\"left\">";
    this.rightSpan = "<span class=\"right\">";
    for(Diff d: deltas)
    {
      if(d.operation==Operation.DELETE)
        this.leftSpan += String.format("<span class=\"delete\">%s</span>", d.text);
      else if(d.operation==Operation.INSERT)
        this.rightSpan += String.format("<span class=\"insert\">%s</span>", d.text);      
      else
      {
        this.leftSpan += d.text;
        this.rightSpan += d.text;
      }
    }
    
    leftSpan += "</span>";
    rightSpan += "</span>";
  }
  public String getLeftSpan()
  {
    return this.leftSpan;
  }

  public String getRightSpan()
  {
    return this.rightSpan;
  }
}
