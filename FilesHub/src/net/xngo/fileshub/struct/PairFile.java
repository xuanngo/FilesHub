package net.xngo.fileshub.struct;

public class PairFile implements Comparable<PairFile>
{
  public int similarRate = 0;
  public String fileA = "";
  public String fileB = "";
  
  // Natural order of PairFile: Sort by similarRate in descending order.
  public int compareTo(PairFile pf)
  {
    if(this.similarRate > pf.similarRate)
      return -1;
    else if(this.similarRate < pf.similarRate)
      return 1;
    else
      return 0;
  }  
}
