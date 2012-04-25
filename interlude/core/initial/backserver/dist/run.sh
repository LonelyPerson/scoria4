#!/bin/sh
err=1
until [ $err == 0 ];
do
	/root/java/java6/bin/java -Dfile.encoding=UTF-8 -Xmx256m -cp WebService.jar:./lib/mysql-connector-java-5.1.6-bin.jar webservice.Main > log/webserver.log 2>&1
	err=$?
	sleep 10;
done