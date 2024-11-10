#!/bin/bash


MYDIR=$(dirname $0)
rm -rf ${MYDIR}/local_tomcat/logs/*

CLASSPATH=${MYDIR}/SERVER/apache-tomcat-9.0.48/bin/bootstrap.jar:${MYDIR}/SERVER/apache-tomcat-9.0.48/bin/commons-daemon.jar:${MYDIR}/SERVER/apache-tomcat-9.0.48/bin/tomcat-juli.jar
mkdir -p ${MYDIR}/local_tomcat/logs/
mkdir -p ${MYDIR}/local_tomcat/webapps/
mkdir -p ${MYDIR}/local_tomcat/temp/
mkdir -p ${MYDIR}/local_tomcat/work/

pid=`fuser 8000/tcp 2>/dev/null | tr -d '[:blank:]'` && test -z "$pid" || { echo "killing $pid"; kill -SIGKILL $pid; }
# Didn't want to do apache's complex config startup (catalina.sh is very unnecessarily complex), 
#so I replaced the runtime ps -ef from linux to make a generic script, -Dcatalina.base/home are set to the appropriate classpath/directories/etc
java -Dcatalina.base=${MYDIR}/local_tomcat \
	-Dcatalina.home=${MYDIR}/SERVER/apache-tomcat-9.0.48 \
	--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED \
	--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED \
	--add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED \
	-Dfile.encoding=UTF-8 \
	-classpath "${CLASSPATH}" \
	org.apache.catalina.startup.Bootstrap start

