# View all files in fileshub.jar
FILESHUB_JAR=fileshub.jar
FILESHUB_TMP_DIR=fileshub

if [ ! -f ${FILESHUB_JAR} ]
then
  echo "ERROR: ${FILESHUB_JAR} is not found!"
else

  rm -rf ${FILESHUB_TMP_DIR}
	mkdir ${FILESHUB_TMP_DIR}
	cp ${FILESHUB_JAR} ${FILESHUB_TMP_DIR}
	cd ${FILESHUB_TMP_DIR}
	jar xf ${FILESHUB_JAR}
	rm -f ${FILESHUB_JAR}
	cd ..
	clear
	find ${FILESHUB_TMP_DIR} -type f | sort
            
fi


