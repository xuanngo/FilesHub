package net.xngo.fileshub;

import java.io.File;

import net.xngo.fileshub.report.Chronometer;

public class Config
{
  public static final String NAME               = "FilesHub";
  public static final String HOME_DIR           = System.getProperty(NAME+".home");
  public static final String DB_FILE_PATH       = HOME_DIR+File.separator+NAME+".db";         // File.separator might be double depending on how dbname.home system property is supplied.
  public static final String HTML_TEMPLATE_PATH = HOME_DIR+File.separator+"template.html";
  public static final String DEBUG              = System.getProperty(NAME+".debug");          // Debug mode: true or false
  public static final int FILES_IN_BATCH        = Integer.parseInt(System.getProperty(NAME+".files.in.batch")); // Number of files to process within a batch.
  
}
