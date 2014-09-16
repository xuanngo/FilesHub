FilesHub
========

FilesHub is used to remove duplicate files.

Usage
======

Add files or directories
```
FilesHub -a FilesHub.db ./ FilesHub*
```

Mark a file is a duplicate of another.
```
FilesHub -a SomeRandomFile.txt FilesHub.db
```

Display the hash of a file
```
FilesHub hash FilesHub.db
```

