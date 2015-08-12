# View all files in fileshub.jar
FILESHUB_JAR=Fileshub.jar
FILESHUB_TMP_DIR=fh_extracted

if [ ! -f ${FILESHUB_JAR} ]
then
  echo "ERROR: ${FILESHUB_JAR} is not found!"
else
  
  # Clean the temporary folder.
  rm -rf ${FILESHUB_TMP_DIR}
	mkdir ${FILESHUB_TMP_DIR}
	
	# Copy jar to temporary folder.
	cp ${FILESHUB_JAR} ${FILESHUB_TMP_DIR}
	
	# Extract jar file. 
	cd ${FILESHUB_TMP_DIR}
	jar xf ${FILESHUB_JAR}
	
	# Delete jar file.
	rm -f ${FILESHUB_JAR}
	cd ..
	
	# List all files in the jar file
	clear
	find ${FILESHUB_TMP_DIR} -type f | sort
            
fi


