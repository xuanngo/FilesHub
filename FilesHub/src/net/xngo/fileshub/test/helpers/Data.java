package net.xngo.fileshub.test.helpers;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class Data
{
  public static File createUniqueFile(final String affix)
  {
    File uniqueFile = null;
    try
    {
      uniqueFile = File.createTempFile(String.format("FilesHubTest_%s_", affix), ".tmp");
      FileUtils.writeStringToFile(uniqueFile, uniqueFile.getName(), true);
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }
    return uniqueFile;
  }
}
