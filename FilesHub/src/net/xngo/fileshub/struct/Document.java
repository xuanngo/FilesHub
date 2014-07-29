package net.xngo.fileshub.struct;

public class Document
{
  public int uid                = 0; // For now uid is an INTEGER. Don't use LONG.
  public String canonical_path  = "";
  public String filename        = "";
  public long last_modified     = 0;
  public String hash            = "";
  public String comment         = "";
}
