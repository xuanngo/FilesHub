CREATE TABLE Shelf (  uid            INTEGER PRIMARY KEY AUTOINCREMENT,
                      canonical_path TEXT NOT NULL,
                      filename       TEXT NOT NULL,
                      last_modified  INTEGER NOT NULL,
                      hash           TEXT,
                      comment        TEXT);
CREATE INDEX shelf_hash ON Shelf (hash);
CREATE INDEX shelf_canonical_path ON Shelf (canonical_path);

CREATE TABLE Trash (  uid            INTEGER PRIMARY KEY AUTOINCREMENT,
                      duid           INTEGER NOT NULL,
                      canonical_path TEXT NOT NULL,
                      filename       TEXT NOT NULL,
                      last_modified  INTEGER NOT NULL,
                      hash           TEXT,
                      comment        TEXT);
CREATE INDEX trash_hash ON Trash (hash);
CREATE INDEX trash_canonical_path ON Trash (canonical_path);