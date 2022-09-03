#!/bin/sh -xeu

cd -- "$(dirname -- "$(realpath -- "$0" || printf %s "$0")")"

cd src
if ! mvn package; then
  mvn clean
  if ! mvn package; then
    mvn package
  fi
fi
