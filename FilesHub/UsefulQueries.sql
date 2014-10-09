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

# Dump
select duid, count(hash) from Trash group by hash having count(hash)>10 ;



