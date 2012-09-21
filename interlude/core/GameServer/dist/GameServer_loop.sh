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
        java -Xbootclasspath/p:./lib/jsr167.jar -Dfile.encoding=UTF-8 -Xms1024m -Xmx1256m -XX:+UseParallelGC -XX:+AggressiveOpts -cp ./lib/*: com.l2scoria.gameserver.GameServer > ./log/stdout.log 2>&1
        sleep 10;
done
