REM sqlite3 FilesHub.db -column -header "SELECT * FROM Document;"
REM sqlite3 FilesHub.db "SELECT * FROM Document;"

REM sqlite3 FilesHub.db "SELECT filename FROM Document ORDER BY filename ASC;"

cls

sqlite3 FilesHub.db -cmd ".width 6 90 25" -column -header "SELECT uid, hash, canonical_path,  FROM Document;"

sqlite3 FilesHub.db -cmd ".width 3 4 90" -column -header "SELECT uid, duid, hash, canonical_path FROM Duplicate;"