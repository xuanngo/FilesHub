Development
============

  Eclipse
  --------
  * In Eclipse, you have to set the followings:
  	* In "Run Configurations->Arguments->VM arguments": 
  	   * -DFilesHub.hash.frequency=13
  	   * -DFilesHub.home=/your-path-of/FilesHub/test/
  	   * -Dlogback.configurationFile=/your-path-of/FilesHub/test/logback.xml
  	- Add all application parameters in "Run Configurations->Arguments->Program arguments"
  * In Eclipse, install TestNG.
      * In Preference, set 'Output directory' to '/test/test-output'.

  * Follow semantic versioning described at http://semver.org/
  * Add new library:
      * In build.xml, add JAR filename to '<attribute name="Rsrc-Class-Path" .../>'.

  Eclipse-Plugins
  ---------------
  * EGit: Distributed source control
      * Install link: http://download.eclipse.org/egit/updates  
  * FindBugs: Scan Java source code and find potential bugs.
      * Install link: http://findbugs.cs.umd.edu/eclipse
  * TestNG: Java unit test framework
      * Install link: http://beust.com/eclipse

  Logging
  ---------------
  WARNING: log 1 line.
  ERROR: log full stack trace.
  
TODO
============
  * Redo reportings: net.xngo.fileshub.report. It was adhoc coding.
      
Libraries
==========
  * Flyway: Upgrade database: http://flywaydb.org/getstarted/download.html
  * Sqlite: Database engine: https://bitbucket.org/xerial/sqlite-jdbc
  * net.xngo.utils.jar: https://github.com/limelime/Java

  * CSV: http://csveed.org/comparison-matrix.html
  * https://code.google.com/p/markdown-doclet/ 
  * https://code.google.com/p/sqlite4java/wiki/ComparisonToOtherWrappers
  * 

Indices
========
CREATE INDEX shelf_hash ON Trash (hash);
CREATE INDEX shelf_canonical_path ON Shelf (canonical_path);
CREATE INDEX trash_hash ON Trash (hash);
CREATE INDEX trash_canonical_path ON Trash (canonical_path);

.indices

================================================
Unicode filename not found: 
  * http://stackoverflow.com/questions/3072376/how-can-i-open-files-containing-accents-in-java
  * http://jonisalonen.com/2012/java-and-file-names-with-invalid-characters/
  * http://stackoverflow.com/questions/3610013/file-listfiles-mangles-unicode-names-with-jdk-6-unicode-normalization-issues
  * http://stackoverflow.com/questions/13513652/encoding-issue-on-filename-with-java-7-on-osx-with-jnlp-webstart
  

