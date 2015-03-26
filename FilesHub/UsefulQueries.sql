# Find broken integrity link between Shelf & Trash: Trash.duid <> Shelf.uid.
SELECT * FROM Trash LEFT JOIN Shelf ON Trash.duid = Shelf.uid WHERE Shelf.uid IS NULL;


# Find duplicate rows
SELECT hash, COUNT(*), canonical_path FROM Shelf GROUP BY hash, canonical_path HAVING COUNT(*) > 1;
SELECT hash, COUNT(*), canonical_path FROM Trash GROUP BY hash, canonical_path HAVING COUNT(*) > 1;

sqlite3 FilesHub.db -cmd ".width 10 5 125" -column -header "SELECT hash, COUNT(*), canonical_path FROM Shelf GROUP BY hash, canonical_path HAVING COUNT(*) > 1;"
sqlite3 FilesHub.db -cmd ".width 6 10 5 125" -column -header "SELECT duid, hash, COUNT(*), canonical_path FROM Trash GROUP BY hash, canonical_path HAVING COUNT(*) > 1;"
sqlite3 FilesHub.db "SELECT COUNT(*) FROM Trash;" ==>45469

# Delete duplicates in Sqlite
DELETE   FROM Trash
WHERE    rowid NOT IN
         (
         SELECT  min(rowid)
         FROM    Trash
         GROUP BY
            duid, 
            hash,
            canonical_path
         );

sqlite3 FilesHub.db "DELETE FROM Trash WHERE rowid NOT IN ( SELECT  min(rowid) FROM Trash GROUP BY duid, hash, canonical_path );"
sqlite3 FilesHub.db -cmd ".width 6 10 5 125" -column -header "SELECT duid, hash, COUNT(*), canonical_path FROM Trash GROUP BY hash, canonical_path HAVING COUNT(*) > 1;"
         

# Delete same document in Shelf and Trash
sqlite3 FilesHub.db -cmd ".width 6 125" -column -header "SELECT Shelf.uid, Shelf.canonical_path, Trash.canonical_path FROM Shelf LEFT JOIN Trash ON Shelf.canonical_path=Trash.canonical_path WHERE Trash.canonical_path IS NOT NULL"
sqlite3 FilesHub.db -cmd ".width 6 125" -column -header "DELETE FROM Trash WHERE canonical_path IN (SELECT Shelf.canonical_path FROM Shelf LEFT JOIN Trash ON Shelf.canonical_path=Trash.canonical_path WHERE Trash.canonical_path IS NOT NULL)"
sqlite3 FilesHub.db -cmd ".width 6 125" -column -header "SELECT Shelf.uid, Shelf.canonical_path, Trash.canonical_path FROM Shelf LEFT JOIN Trash ON Shelf.canonical_path=Trash.canonical_path WHERE Trash.canonical_path IS NOT NULL"

# Delete orphan documents: duid in Trash but not Shelf
sqlite3 -column -header -cmd ".width 6 125" FilesHub.db "SELECT Trash.duid, Trash.canonical_path FROM Trash LEFT JOIN Shelf ON Trash.duid=Shelf.uid WHERE Shelf.uid IS NULL"
sqlite3 FilesHub.db "DELETE FROM Trash WHERE duid IN (SELECT Trash.duid FROM Trash LEFT JOIN Shelf ON Trash.duid=Shelf.uid WHERE Shelf.uid IS NULL)"

# Delete duplicate hashes.
sqlite3 FilesHub.db "SELECT hash, COUNT(*), canonical_path FROM Shelf GROUP BY hash HAVING COUNT(*) > 1;"
sqlite3 FilesHub.db "DELETE FROM Shelf WHERE hash IN (SELECT hash FROM Shelf GROUP BY hash HAVING COUNT(*) > 1);"
sqlite3 FilesHub.db "SELECT hash, COUNT(*), canonical_path FROM Shelf GROUP BY hash HAVING COUNT(*) > 1;"

# Find the longest canonical_path
sqlite3 FilesHub.db "select length(canonical_path), * from Shelf order by length(canonical_path) desc limit 1;" | wc -c
sqlite3 FilesHub.db "select length(canonical_path), * from Trash order by length(canonical_path) desc limit 1;" | wc -c

Assuming 100 MB of heap memory and average Document size = 400 bytes(largest = 493 bytes).
Number of possible in heap=100*1024*1024/400 = 262,144

# Display number of entry without size.
sqlite3 FilesHub.db "select count(*) from Shelf where size < 1;"
sqlite3 FilesHub.db "select count(*) from Trash where size < 1;"

# Dump
select duid, count(hash) from Trash group by hash having count(hash)>10 ;


sqlite3 FilesHub.db "DELETE FROM Trash WHERE duid=5;"
