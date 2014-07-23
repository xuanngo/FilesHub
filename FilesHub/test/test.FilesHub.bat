REM sqlite3 FilesHub.db -column -header "SELECT * FROM Document;"
REM sqlite3 FilesHub.db "SELECT * FROM Document;"

sqlite3 FilesHub.db "SELECT filename FROM Document ORDER BY filename ASC;"

sqlite3 FilesHub.db -cmd ".width 6 90 25" -column -header "SELECT uid, canonical_path, hash FROM Document;"