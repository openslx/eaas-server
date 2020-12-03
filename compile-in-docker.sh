#!/bin/sh
BASEDIR="$(dirname "$(readlink -f -- "$0")")"

cd "$BASEDIR"
cd src

# We want to preserve correct file owners in host filesystem,
# so we have to create `.m2/` ourself (`docker run` would create it as root).
mkdir .m2 2> /dev/null

# Because user is not in `/etc/passwd`, calling `mvn` inside the container
# will end up putting the "real" `.m2/` into another `?/` directory, though.
ln -s . "?" 2> /dev/null

docker run --rm -v "$PWD":/root -v "$PWD"/.m2:/root/.m2 \
  -u "$(id -u):$(id -g)" -e "HOME=/root" -v "$PWD":/src -w /src \
  -it maven:3-jdk-11 mvn package "$@"

mkdir "$BASEDIR"/deployments 2> /dev/null
cp ear/target/eaas-server.ear "$BASEDIR"/deployments/
