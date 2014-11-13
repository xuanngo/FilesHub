package net.xngo.fileshub.upgrade;

import java.io.File;

import net.xngo.fileshub.Config;
import net.xngo.fileshub.Main;
import net.xngo.fileshub.Utils;
import net.xngo.fileshub.db.Connection;


import net.xngo.utils.java.io.FileUtils;
import net.xngo.utils.java.time.CalUtils;
import net.xngo.utils.java.time.Chronometer;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;

/**
 * IMPORTANT: SQLite only supports a single connection at a time.
 *            Therefore, you need to close and open connection every time
 *            you use Main.connection.
 *
 * @author Xuan Ngo
 *
 */
public class Upgrade
{

  private String dbFilePath = Config.DB_FILE_PATH;
  private String sqlDir     = Config.SQL_DIR.replace('\\', '/');
  private String dbUrl      = Config.DB_URL;
  
  private Flyway flyway = new Flyway();

  public Upgrade()
  {
    
    Main.connection.close(); 
    
    // Set your datasource.
    this.flyway.setDataSource(this.dbUrl, null, null);
    
    // Set the location of all your SQL files: V?__*.sql
    this.flyway.setLocations("filesystem:"+this.sqlDir);
    
    this.flyway.setInitDescription("Fileshub");
    
    // Force the creation of 'schema_version' table on existing database.
    this.flyway.setInitOnMigrate(true);  
  }
  
  public void run()
  {
    Chronometer chrono = new Chronometer();
          chrono.start();
    this.backupBeforeUpgrade();
          chrono.stop("Backup database file");
    this.version0001Run();
          chrono.stop("Migrate to version 1");
    this.version0002Run();
          chrono.stop("Migrate to version 2");
          chrono.display("Migration Runtime");
  }
  
  /**
   * Add size column.
   */
  private void version0002Run()
  {
    Main.connection = new Connection();
    boolean shelfSize = Main.connection.isColumnExists("Shelf", "size");
    boolean trashSize = Main.connection.isColumnExists("Trash", "size");
    Main.connection.close();
    
    if(!shelfSize && !trashSize)
    {
      // Add size column.
      MigrationVersion migrationVersion = MigrationVersion.fromVersion("2");
      this.flyway.setTarget(migrationVersion);
      this.flyway.migrate(); // Migrate up to version set in setTarget().
      
      // Update file size.
      Version0002 version2 = new Version0002();
      version2.run();
    }
    else if(shelfSize && trashSize)
    {// Column Shelf.size & Trash.size exist.
      // Update file size.
      Version0002 version2 = new Version0002();
      version2.run();
    }
    else
    {
      System.out.println("ERROR: Can't upgrade to Version 2. Both columns Shelf.size and Trash.size should exist.");
      System.exit(0);
    }
  }
  
  /**
   * Initial database structure.
   */
  private void version0001Run()
  {
    MigrationVersion migrationVersion = MigrationVersion.fromVersion("1");
    this.flyway.setTarget(migrationVersion);
    this.flyway.migrate(); // Migrate up to version set in setTarget().      
  }  
  
  
  private void backupBeforeUpgrade()
  {
    File dbFile = new File(this.dbFilePath);
    if(dbFile.exists())
    {
      // Create backup directory.
      File backupDir = new File(Config.HOME_DIR+File.separator+"backupdb");
      backupDir.mkdir();
      
      // Compress db file and put it in backup directory.
      String source = this.dbFilePath;
      String destination = backupDir.getAbsolutePath()+File.separator+dbFile.getName()+"_"+CalUtils.toString("yyyy-MM-dd_HH.mm.ss")+".zip";

      System.out.println(String.format("Backup your database file to %s ....", destination));
      FileUtils.zip(source, destination);
    }
    
  }
}
