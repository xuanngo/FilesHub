package net.xngo.fileshub.test.helpers;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class Data
{
  public static File createUniqueFile()
  {
    File uniqueFile = null;
    try
    {
      uniqueFile = File.createTempFile("FilesHubTestFile", ".tmp");
      FileUtils.writeStringToFile(uniqueFile, uniqueFile.getName(), true);
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    return uniqueFile;
  }
}
