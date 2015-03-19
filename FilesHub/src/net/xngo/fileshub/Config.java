package net.xngo.fileshub;

import java.io.File;

public class Config
{
  // Mandatory.
  public static final String NAME               = "FilesHub";
  public static final String HOME_DIR           = System.getProperty(NAME+".home");
  public static final String DB_FILE_PATH       = HOME_DIR+File.separator+NAME+".db";         // File.separator might be double depending on how dbname.home system property is supplied.
  public static final String HTML_TEMPLATE_PATH = HOME_DIR+File.separator+"template.html";
  public static final String JDBC_CLASSLOADER   = "org.sqlite.JDBC";
  public static final String DB_URL             = "jdbc:sqlite:/"+DB_FILE_PATH.replace('\\', '/'); // Not efficient. Use File.toURI().toURL().toString();  
  public static final String SQL_DIR            = HOME_DIR+File.separator+"upgrade"+File.separator+"sql";
  
  // Optional.
  public static final String WORD_LIST          = HOME_DIR+File.separator+"words.lst";
  
}
