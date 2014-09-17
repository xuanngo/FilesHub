package net.xngo.fileshub;

import java.util.LinkedList;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Operation;

public class Difference
{
  
  private LinkedList<Diff> deltas = null;
  private String leftSpan="";
  private String rightSpan="";

  public Difference(String left, String right)
  {
    diff_match_patch diffMatchPatch = new diff_match_patch();
    this.deltas = diffMatchPatch.diff_main(left, right);
    diffMatchPatch.diff_cleanupSemantic(this.deltas);
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
