#!/usr/bin/env bash
#set($dollar = '$')
set -e

PROFILE=production
cd "${dollar}(dirname "${dollar}0")"
mvn clean verify -P${dollar}{PROFILE} -DskipTests  && mvn -P${dollar}{PROFILE},cargo.run -pl . cargo:configure cargo:package

cat > target/package/bin/setenv.sh << EOF

export WEBOOT_CFG_LOCATION="conf/${rootArtifactId}-dev-configuration.xml"
export JAVA_OPTS="-Xmx1024m -XX:+HeapDumpOnOutOfMemoryError"

EOF

cat > target/package/run.sh << EOF
#!/usr/bin/env bash
cd "\\${dollar}(dirname "${dollar}0")"
bin/catalina.sh run
EOF

cp ./README.md target/package
chmod u+x target/package/bin/*.sh
chmod u+x target/package/*.sh

(cd target/ && tar -C ./package -czf ./${rootArtifactId}-distr-${dollar}{PROFILE}.tar.gz ./)

echo ""
echo ""
echo "--------------------------------------------------------------------"
echo "Project distributive archive: target/${rootArtifactId}-distr-${dollar}{PROFILE}.tar.gz"
echo ""

