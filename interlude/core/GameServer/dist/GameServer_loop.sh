#!/bin/bash

DBHOST=localhost
USER=root
PASS=
DBNAME=l2jdb
ulimit -n 65535

err=1
until [ $err == 0 ];
do
        #mysqlcheck -h $DBHOST -u $USER --password=$PASS -s -r $DBNAME>>"log/`date +%Y-%m-%d_%H:%M:%S`-sql_check.log"
        #mysqldump -h $DBHOST -u $USER --password=$PASS $DBNAME|zip "backup/`date +%Y-%m-%d_%H:%M:%S`-l2jdb_gameserver.zip" -
        mv ./log/java0.log.0 "./log/`date +%Y-%m-%d_%H-%M-%S`_java.log"
        mv ./log/stdout.log "./log/`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
      #if need - uncoment#  #mv ./log/chat.log "./log/`date +%Y-%m-%d_%H:%M:%S`-chat.log"#
        java -Xbootclasspath/p:./lib/jsr167.jar -Xms512m -Xmx1024m -XX:+UseParallelGC -XX:+AggressiveOpts -cp ./lib/*:l2scoria-mmocore-2.jar:bsh-engine.jar:bsh-2.0b5.jar:c3p0-0.9.1.2.jar:jython.jar:jython-engine.jar:commons-logging-1.1.jar:commons-io-1.2.jar:commons-lang-2.1.jar:javolution.jar:akumu.geoengine.1.2.0.7.jar:trove-2.1.0.jar:ExtAPI.jar:log4j-1.2.17.jar:lameguard-1.9.5.jar:mysql-connector-java-5.1.8-bin.jar:/lib/l2scoria-core-3.jar com.l2scoria.gameserver.GameServer > ./log/stdout.log 2>&1
        sleep 10;
done
