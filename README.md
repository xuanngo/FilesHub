FilesHub
========

FilesHub is used to find duplicate files.

Usage
======

Add files or directories
```
# FilesHub -a <files, directories or mix>
FilesHub -a FilesHub.db ./ FilesHub*
```

Mark a file is a duplicate of another.
```
# FilesHub -d <This file.txt> <Is a duplicate of this file.txt>
FilesHub -d SomeRandomFile.txt FilesHub.db
```

Compute the hash of files
```
# FilesHub hash <files, directories or mix>
FilesHub hash FilesHub.db ./ FilesHub*
```

Search by unique identifier(UID)
```
FilesHub search -uid 2
```

Search by hash value
```
FilesHub search -h "-943432"
```

Search by filename: Can use wildcard(*)
```
FilesHub search -f "*fileshub*"
```

Output
======
When using '-a' option, FilesHub will also save duplicate file paths in CSV and HTML format automatically in the executing directory. They have the following filename pattern:
* CSV: results_<directories>_<timestamp>.csv
* HTML: results_<directories>_<timestamp>.html
