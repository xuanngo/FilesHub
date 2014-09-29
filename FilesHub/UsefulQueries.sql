# Find duplicate rows
SELECT hash, canonical_path, COUNT(*) FROM Shelf GROUP BY hash, canonical_path HAVING COUNT(*) > 1;
SELECT hash, canonical_path, COUNT(*) FROM Trash GROUP BY hash, canonical_path HAVING COUNT(*) > 1;