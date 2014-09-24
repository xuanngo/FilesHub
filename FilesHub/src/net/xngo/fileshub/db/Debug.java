package net.xngo.fileshub.db;

import net.xngo.fileshub.Config;

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
    
    if(Config.DEBUG!=null)
      debug = Boolean.parseBoolean(Config.DEBUG);    
    return debug;
  }  
}
