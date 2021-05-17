cd src
docker run --rm -v %cd%:/root -v %cd%:/src -w /src -i maven:3-jdk-11 mvn package
scp ear/target/eaas-server.ear ubuntu@historic-builds.emulation.cloud:/eaas-home/deployments/
