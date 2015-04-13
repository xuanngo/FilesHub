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
# fh -a <files, directories or mix>
fh -a FilesHub.db ./ log*
```

Mark a file is a duplicate of another.
```
# fh -d <This file.txt> <Is a duplicate of this file.txt>
fh -d SomeRandomFile.txt FilesHub.db
```

Compute the hash of files
```
# fh hash <files, directories or mix>
fh hash FilesHub.db ./ FilesHub*
```

Search by unique identifier(UID)
```
fh search -uid 2
```

Search by hash value
```
fh search -h "-943432"
```

Search by filename: Can use wildcard(*)
```
fh search -f "*fileshub*"
```

Search by file path: Can use wildcard(*)
```
fh search -p "*\somewhere\fileshub\*"
```

Search similar files of the current directory and its sub-directories against the whole database. It will output the results in potentialDuplicates.html.
```
# Search all file names that are 80% similar or more.
fh search -s 80
```

Output
======
When using '-a' option, FilesHub will save the results in `results_<directories>_<timestamp>.html` from the executing directory.