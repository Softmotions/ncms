#!/bin/sh
cd $(dirname $(readlink -f $0))
mvn -pl ncms-engine-web tomcat7:run