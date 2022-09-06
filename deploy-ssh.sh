#!/bin/sh -eu

if test "$#" -lt 1; then
  cat << EOF
Usage: $0 username@remoteServer
EOF
  return 2
fi

cd -- "$(dirname -- "$(realpath -- "$0" || printf %s "$0")")"

set -x
remote="$1"

rsync -zv --progress --rsync-path="sudo rsync" -- src/ear/target/eaas-server.ear "$remote:/eaas-home/deployments/"
ssh -- "$remote" sudo systemctl restart eaas

printf '\nssh %s\nhttps://%s\n' "$remote" "${remote##*@}"
