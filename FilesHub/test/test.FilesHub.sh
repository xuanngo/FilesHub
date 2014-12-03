#!/bin/bash

clear 

echo "========================="
echo "Shelf:"
sqlite3 FilesHub.db -cmd ".width 11 15 15 10 125" -column -header "SELECT uid, hash, last_modified, size, canonical_path FROM Shelf;"

echo ""
echo "========================="
echo "Trash:"
sqlite3 FilesHub.db -cmd ".width 11 15 15 10 4 125" -column -header "SELECT duid, hash, last_modified, size, uid, canonical_path FROM Trash;"



echo ""
echo "========================="
echo "Schema:"
sqlite3 FilesHub.db -cmd ".width 13 15 11 25 11 25 11 13 24 15 11" -column -header "SELECT * from schema_version;"
