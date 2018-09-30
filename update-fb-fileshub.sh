#!/bin/bash
set -e
# Description: Update Firstboot fileshub script.

fb_run_dir=$(readlink -ev /media/master/github/firstboot/firstboot/apps/fileshub/run)
fileshub_dir=$(readlink -ev ./releases/latest/)

# Update clean fileshub to firstboot.
  rm -rf "${fb_run_dir}"
  mkdir -p "${fb_run_dir}"
  yes | cp -a "${fileshub_dir}"/. "${fb_run_dir}"

# Commit fileshub at firstboot.
  (
    cd "${fb_run_dir}"
    # Git commands execution order is important.
    git ls-files --deleted -z | xargs -r -0 git rm && git commit -m 'fileshub: commit deleted files.' || true
    git ls-files --modified -z | xargs -r -0 git commit -m 'fileshub: commit changed files.'
    git ls-files --others -z | xargs -r -0 git add && git commit -m 'fileshub: commit new files.' || true
  )  