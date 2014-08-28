REM sqlite3 FilesHub.db -column -header "SELECT * FROM Shelf;"
REM sqlite3 FilesHub.db "SELECT * FROM Shelf;"

REM sqlite3 FilesHub.db "SELECT filename FROM Shelf ORDER BY filename ASC;"

REM sqlite3 FilesHub.db -cmd ".width 4 15 15 125" -column -header "SELECT uid, hash, last_modified, canonical_path FROM Shelf LIMIT 100;"

REM sqlite3 FilesHub.db -cmd ".width 4 15 15 4 125" -column -header "SELECT duid, hash, last_modified, uid, canonical_path FROM Trash LIMIT 100;"

cls

sqlite3 FilesHub.db -cmd ".width 4 15 15 125" -column -header "SELECT uid, hash, last_modified, canonical_path FROM Shelf;"

sqlite3 FilesHub.db -cmd ".width 4 15 15 4 125" -column -header "SELECT duid, hash, last_modified, uid, canonical_path FROM Trash;"