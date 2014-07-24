REM sqlite3 FilesHub.db -column -header "SELECT * FROM Document;"
REM sqlite3 FilesHub.db "SELECT * FROM Document;"

REM sqlite3 FilesHub.db "SELECT filename FROM Document ORDER BY filename ASC;"

cls

sqlite3 FilesHub.db -cmd ".width 4 15 125" -column -header "SELECT uid, hash, canonical_path FROM Document;"

sqlite3 FilesHub.db -cmd ".width 4 15 4 125" -column -header "SELECT duid, hash, uid, canonical_path FROM Duplicate;"