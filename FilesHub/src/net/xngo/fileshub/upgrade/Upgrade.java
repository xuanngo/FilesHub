package net.xngo.fileshub.upgrade;

import java.io.File;

import net.xngo.fileshub.Config;
import net.xngo.fileshub.Utils;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;

public class Upgrade
{
  private Flyway flyway = new Flyway();
  
  public Upgrade()
  {
    // Set your datasource.
    this.flyway.setDataSource(Config.DB_URL, null, null);
    
    // Set the location of all your SQL files: V?__*.sql
    this.flyway.setLocations("filesystem:"+Config.SQL_DIR);
    
    // Force the creation of 'schema_version' table on existing database.
    this.flyway.setInitOnMigrate(true);    
  }
  public void run()
  {
    this.backupBeforeUpgrade();
    this.version0001();
    this.version0002();
  }
  
  /**
   * Add size column.
   */
  private void version0002()
  {
    // Add size column.
    MigrationVersion migrationVersion = MigrationVersion.fromVersion("2");
    flyway.setTarget(migrationVersion);
    flyway.migrate(); // Migrate up to version set in setTarget().
    
    // Update file size.
    Version0002 version2 = new Version0002();
    version2.run();
  }
  
  /**
   * Initial database structure.
   */
  private void version0001()
  {
    MigrationVersion migrationVersion = MigrationVersion.fromVersion("1");
    flyway.setTarget(migrationVersion);
    flyway.migrate(); // Migrate up to version set in setTarget().      
  }  
  
  
  private void backupBeforeUpgrade()
  {
    File dbFile = new File(Config.DB_FILE_PATH);
    File backupFile = new File(Config.DB_FILE_PATH+"."+System.currentTimeMillis()+".bck");
    Utils.copyFileUsingFileChannels(dbFile, backupFile);
  }
}
