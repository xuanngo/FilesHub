Development
============
  -In Eclipse, you have to set the following in "Run Configurations->Arguments->VM arguments": -DFilesHub.home=<your path>\FilesHub\FilesHub\releases\latest\
  -Follow semantic versioning described at http://semver.org/
  -Add new library:
      --In build.xml, add JAR filename to '<attribute name="Rsrc-Class-Path" .../>'.