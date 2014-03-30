#!/bin/sh
cd $(dirname $(readlink -f $0))
mvn -pl ncms-engine-web liquibase:update tomcat7:run
