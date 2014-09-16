package net.xngo.fileshub.db;

public class Debug
{
  private static boolean debug = false;
  
  /**
   * Display debug message.
   * @param s
   */
  public static void msg(String s)
  {
    if(Debug.activate())
      System.out.println("DEBUG: "+s);
  }
  
  /**
   * 
   * @return True is debug mode is ON. Otherwise, false.
   */
  public static boolean activate()
  {
    String sDebug = System.getProperty(Conn.DB_NAME+".debug");
    if(sDebug!=null)
      debug = Boolean.parseBoolean(sDebug);    
    return debug;
  }  
}
