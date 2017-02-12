#!/usr/bin/env bash
mvn -Pprerelease clean verify && mvn -Pprerelease,cargo.run -Dcargo.debug.suspend=n

