#!/bin/bash
# Description: Scan source code to find whether logger declaration is correct or not.
#     The copy and paste of 
#       final static Logger log = LoggerFactory.getLogger(MyClasName.class);
#     with the wrong class name.


WORK_DIR=./src/


for file in $( find ${WORK_DIR} -type f )
do
  
  PREFIX_LOGGER_DECLARATION="LoggerFactory.getLogger("
  
  # If logger declaration is found.
	if grep -q ${PREFIX_LOGGER_DECLARATION} ${file}
	then
	  
	  # Get class name.
		filename=$(basename "${file}")
		extension="${filename##*.}"
		classname="${filename%.*}"
		
		CORRECT_LOGGER_DECLARATION="${PREFIX_LOGGER_DECLARATION}${classname}.class);"
		if ! grep -q ${CORRECT_LOGGER_DECLARATION} ${file}
		then
		  ACTUAL_DECLARATION=$(grep ${PREFIX_LOGGER_DECLARATION} ${file})
      echo "ERROR: Wrong class name in ${file}."
      echo "   Used: ${ACTUAL_DECLARATION}"
      echo "   Should: ${CORRECT_LOGGER_DECLARATION}"
    fi
    
	fi  
done