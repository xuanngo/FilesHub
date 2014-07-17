
/* Drop Tables */

DROP TABLE Document;




/* Create Tables */

-- Document is used as table name because File is a keyword.
CREATE TABLE Document
(
	-- Unique ID
	UID text NOT NULL UNIQUE,
	-- Location is used as column name because path is a keyword.
	Location text,
	Filename text,
	-- File size in bytes
	Size integer,
	PRIMARY KEY (UID)
);



