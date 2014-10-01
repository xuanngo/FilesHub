:: Description: Display megabytes files only.
:: Author: Xuan Ngo

SET filename=%1
SET outputFilename=%2

:: Delete line with [ KB]        | Delete line with [ byte]| Delete line with [ bytes]| Delete line with less than 100 MB.
sed "s/.* KB\x3C.*//" %filename% | sed "s/.* byte\x3C.*//" | sed "s/.* bytes\x3C.*//" | sed -r "s/.*\x3E[0-9][0-9]?\.?[0-9]* MB\x3C.*//" | sed -e "/^$/d"   > %outputFilename%
