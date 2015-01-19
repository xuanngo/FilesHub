package net.xngo.fileshub.cmd;


import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;


@Parameters(commandDescription = "Search by different attributes.")
public class CmdSearch
{
  public static String name = "search";
  
  @Parameter(names = {"-id", "--id"}, description = "Search by document ID.", arity = 1)
  public int id;
  
  @Parameter(names = {"-h", "--hash"}, description = "Search by hash.", arity = 1)
  public String hash;
  
  @Parameter(names = {"-f", "--filename"}, description = "Search by filename. Use * as wildcard.", arity = 1)
  public String filename;
  
  @Parameter(names = {"-p", "--path"}, description = "Search by file path. Use * as wildcard.", arity = 1)
  public String filepath;
  
  @Parameter(names = {"-s", "--similar"}, description = "Search all similar files of the current directory and its subdirectories "
                                                          + "against the database. \nUse number(0-100) as the similarity rate. E.g. 80 means return filenames that are 80% similar or more.")
  public int fuzzyRate=80;
  
}
