package net.xngo.fileshub.db;

public class Debug
{
  private static boolean debug = false;
  

  
  public static void msg(String s)
  {
    if(Debug.activate())
      System.out.println("DEBUG: "+s);
  }
  
  private static boolean activate()
  {
    String sDebug = System.getProperty(Conn.DB_NAME+".debug");
    if(sDebug!=null)
      debug = Boolean.parseBoolean(sDebug);    
    return debug;
  }  
}
