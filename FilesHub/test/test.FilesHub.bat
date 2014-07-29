REM sqlite3 FilesHub.db -column -header "SELECT * FROM Repository;"
REM sqlite3 FilesHub.db "SELECT * FROM Repository;"

REM sqlite3 FilesHub.db "SELECT filename FROM Repository ORDER BY filename ASC;"

cls

sqlite3 FilesHub.db -cmd ".width 4 15 15 125" -column -header "SELECT uid, hash, last_modified, canonical_path FROM Repository;"

sqlite3 FilesHub.db -cmd ".width 4 15 15 4 125" -column -header "SELECT duid, hash, last_modified, uid, canonical_path FROM Trash;"