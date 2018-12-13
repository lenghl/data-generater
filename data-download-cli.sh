#!/bin/bash

#setting locale evn
export LANG=zh_CN.UTF-8
export LC_ALL=zh_CN.UTF-8

NOHUP=${NOHUP:=$(which nohup)}

#####change by zhenjie.wang#####
function is_file_contain_key() {
  file=$1
  key=$2
  is_contain=0
  while read line
  do
    if [[ $line =~ $key ]]; then
      is_contain=1
      break
    fi
  done < $file
  echo $is_contain
}

cronTask(){
    file=$2'cron'
    touch $file
    crontab -l > $file
    key="/data-download-cli.sh start"
    is_contain=`is_file_contain_key $file "$key"`

    case $1 in
         add)
           if [ $is_contain = 0 ]; then
                echo "*/1 * * * * $2$key" >> $file
           fi
         ;;
    esac
    crontab $file
    rm -rf $file
}

start() {
	origin_pid=`ps -ef | grep data-generater-1.0-SNAPSHOT-jar-with-dependencies.jar | grep -v grep | awk '{print $2}'`
	currentDir=/usr/jdk64

	if [ ! "x$origin_pid" == "x" ]; then
        echo $origin_pid > $currentDir/p.pid
		echo "data-generater-1.0-SNAPSHOT-jar-with-dependencies.jar process has already been running,quit"
		exit 1;
	fi
	
	cronTask 'add' $currentDir

	nohup /usr/jdk64/jdk1.8.0_112/bin/java -jar /usr/jdk64/data-generater-1.0-SNAPSHOT-jar-with-dependencies.jar /usr/jdk64  > /usr/jdk64/log.log 2>&1 &
}


stop(){
	pid=`ps -ef | grep $mainclass|grep -v grep|awk '{print $2}'`
	kill -9 $pid
	echo "[`date +%Y-%m-%d' '%H:%M:%S`] stop mirror-web-api finished.pid:$pid"
}


usage() {
        echo "Usage: `basename $0` (start|stop|restart|status) [-v|--verbose]"
}


#set default value
action=usage
debug=0
args=""

#process args
while [ $# -gt 0 ]; do
        case "$1" in
                start|stop|restart|status)
                        action=$1
                        ;;
                -v|--verbose)
                        verbose=1
                        ;;
                *)
                        args="$args $1"
        esac
        shift
done


case "$action" in
        start)
                start
                ;;
        stop)
                stop
                ;;
esac



