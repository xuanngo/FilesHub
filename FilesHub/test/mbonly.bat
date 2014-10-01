:: Description: Display megabytes files only.
:: Author: Xuan Ngo

SET filename=%1
SET outputFilename=%2
chcp 65001
:: [ KB]                         | [ byte]                 | [ bytes]                 | [ ? MB]                        | Want only > 100s MB ->[ ??.?? MB]
sed "s/.* KB\x3C.*//" %filename% | sed "s/.* byte\x3C.*//" | sed "s/.* bytes\x3C.*//" | sed "s/.*\x3E[0-9] MB\x3C.*//" | sed "s/.*\x3E[0-9][0-9]*\.*[0-9][0-9]* MB\x3C.*//" > %outputFilename%
