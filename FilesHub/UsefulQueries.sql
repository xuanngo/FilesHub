# Find broken integrity link between Shelf & Trash: Trash.duid <> Shelf.uid.
SELECT * FROM Trash LEFT JOIN Shelf ON Trash.duid = Shelf.uid WHERE Shelf.uid IS NULL;


# Find duplicate rows
SELECT hash, COUNT(*), canonical_path FROM Shelf GROUP BY hash, canonical_path HAVING COUNT(*) > 1;
SELECT hash, COUNT(*), canonical_path FROM Trash GROUP BY hash, canonical_path HAVING COUNT(*) > 1;

sqlite3 FilesHub.db -cmd ".width 15 3 125" -column -header "SELECT hash, COUNT(*), canonical_path FROM Shelf GROUP BY hash, canonical_path HAVING COUNT(*) > 1;"
sqlite3 FilesHub.db -cmd ".width 15 3 125" -column -header "SELECT hash, COUNT(*), canonical_path FROM Trash GROUP BY hash, canonical_path HAVING COUNT(*) > 1;"


# Dump
select duid, count(hash) from Trash group by hash having count(hash)>10 ;