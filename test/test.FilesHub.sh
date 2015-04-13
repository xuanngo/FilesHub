#!/bin/bash

clear 

echo "Shelf:"
sqlite3 -cmd ".width 11 15 15 10 125" -column -header FilesHub.db "SELECT uid, hash, last_modified, size, canonical_path FROM Shelf;"

echo "Trash:"
sqlite3 -cmd ".width 11 15 15 10 4 125" -column -header FilesHub.db "SELECT duid, hash, last_modified, size, uid, canonical_path FROM Trash;"

echo ""
echo "Schema:"
sqlite3 -cmd ".width 13 15 11 25 11 25 11 13 24 15 11" -column -header FilesHub.db "SELECT * from schema_version;"