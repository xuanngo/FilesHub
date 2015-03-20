package net.xngo.fileshub.upgrade;

import java.sql.Connection;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.FlywayCallback;

/**
 * This class define different set of hooks that you can
 *  use to execute your custom code in the upgrading process.
 *  
 * @author Xuan Ngo
 *
 */
public class FileshubCallback  implements FlywayCallback
{
  public void afterClean(Connection connection)
  {}
  public void afterEachMigrate(Connection connection, MigrationInfo info)
  {
    String version = info.getVersion().toString();
    
    // Version 2: Add file size column.
    if(version.compareTo("2")==0)
    {
      // Update file size.
      Version0002 version2 = new Version0002();
      version2.run();
    }
    
    // Version 3: Use different hash function
    
    Upgrade.chrono.stop("Migrate to version "+version);
  }
  public void afterInfo(Connection connection)
  {}
  public void afterInit(Connection connection)
  {}
  public void afterMigrate(Connection connection)
  {}
  public void afterRepair(Connection connection)
  {}
  public void afterValidate(Connection connection)
  {}
  public void beforeClean(Connection connection)
  {}
  public void beforeEachMigrate(Connection connection, MigrationInfo info)
  {}
  public void beforeInfo(Connection connection)
  {}
  public void beforeInit(Connection connection)
  {}
  public void beforeMigrate(Connection connection)
  {}
  public void beforeRepair(Connection connection)
  {}
  public void beforeValidate(Connection connection)
  {}
}
