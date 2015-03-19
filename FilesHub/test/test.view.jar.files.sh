# View all files in fileshub.jar
FILESHUB_TMP_DIR=fileshub

rm -rf ${FILESHUB_TMP_DIR}
mkdir ${FILESHUB_TMP_DIR}
cp ${FILESHUB_TMP_DIR}.jar ${FILESHUB_TMP_DIR}
cd ${FILESHUB_TMP_DIR}
jar xf ${FILESHUB_TMP_DIR}.jar
rm -f ${FILESHUB_TMP_DIR}.jar
cd ..
clear
find ${FILESHUB_TMP_DIR} -type f | sort

