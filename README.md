FilesHub
========

FilesHub is used to find duplicate files.

Requirements
=============
* Java 7


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

Compute the [hash](https://code.google.com/p/xxhash/) of files
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

Search by file path: Can use wildcard(*)
```
FilesHub search -p "*\somewhere\fileshub\*"
```

Search similar files from the current directory and its sub-directories. It will output the results in potentialDuplicates.html.
```
# Search all file names that are 80% similar or more.
FilesHub search -s 80
```

Output
======
When using '-a' option, FilesHub will save the results in `results_<directories>_<timestamp>.html` from the executing directory.