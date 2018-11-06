#!/bin/bash
set -e
# Description: Copy FilesHub.db to RAM filesystem / local filesystem.
script_name=$(basename "$0")

action=$1

ram_fh_dir=/dev/shm/fileshub

case "${action}" in
	ram|mem)
		if [ -d "${ram_fh_dir}" ]; then
			echo "Error: ${ram_fh_dir} directory exists. FilesHub.db is already in memory. Aborted!"
			echo "  e.g. ./${script_name} ram|back"
			exit 1;
		else
			mkdir -p "${ram_fh_dir}"											&&
			cp -u "${FILESHUB_HOME}/FilesHub.db" "${ram_fh_dir}"				&&
			ln -sf "${ram_fh_dir}/FilesHub.db" "${FILESHUB_HOME}/FilesHub.db"	&&
			echo "FilesHub.db is copied to ${ram_fh_dir}/FilesHub.db."
		fi			
	;;
	
	back)
		if [ -d "${ram_fh_dir}" ]; then
			cp -u "${ram_fh_dir}/FilesHub.db" "${FB_FILESHUB_DIR}" 					&&
			ln -sf "${FB_FILESHUB_DIR}/FilesHub.db" "${FILESHUB_HOME}/FilesHub.db" 	&&
			rm -rf "${ram_fh_dir}"													&&
			echo "FilesHub.db is copied back to ${FB_FILESHUB_DIR}/FilesHub.db."
		else
			echo "Error: ${ram_fh_dir} directory doesn't exist. FilesHub.db is not in memory. Aborted!"
			echo "  e.g. ./${script_name} ram|back"
			exit 1;
		fi
	;;
		
	*)
		echo "Error: Unknown action=>${action}"
		echo "  e.g. ./${script_name} ram|back"
		exit 1
	;;
esac
