REM sqlite3 FilesHub.db -column -header "SELECT * FROM Shelf;"
REM sqlite3 FilesHub.db "SELECT * FROM Shelf;"

REM sqlite3 FilesHub.db "SELECT filename FROM Shelf ORDER BY filename ASC;"

REM sqlite3 FilesHub.db -cmd ".width 11 15 15 125" -column -header "SELECT uid, hash, last_modified, canonical_path FROM Shelf LIMIT 100;"

REM sqlite3 FilesHub.db -cmd ".width 11 15 15 4 125" -column -header "SELECT duid, hash, last_modified, uid, canonical_path FROM Trash LIMIT 100;"

cls

sqlite3 FilesHub.db -cmd ".width 11 15 15 10 125" -column -header "SELECT uid, hash, last_modified, size, canonical_path FROM Shelf;"

sqlite3 FilesHub.db -cmd ".width 11 15 15 10 4 125" -column -header "SELECT duid, hash, last_modified, size, uid, canonical_path FROM Trash;"

sqlite3 FilesHub.db -cmd ".width 13 15 11 25 11 25 11 13 24 15 11" -column -header "SELECT * from schema_version;"