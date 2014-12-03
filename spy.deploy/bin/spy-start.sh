#!/bin/bash

##================ setting args ===============
chmod +x base_fun.sh
. ./base_fun.sh

TASK_NAME="spy" 

CONSOLE_PATH=$BASE/../output
mkdir -p $CONSOLE_PATH
OUTPUT_FILE=$CONSOLE_PATH/commonTask.log 

##=============================================
if [ "$1" = "start" ] ; then
  if ! ps -ef |grep 'java' |grep "$TASK_NAME"
  then
    ##================ running ====================
    echo "`date`:run --------------------------------" >> $OUTPUT_FILE
    nohup java $JAVA_OPTS -cp $CLASSPATH com.suning.app.spy.core.Agent  1>>$OUTPUT_FILE 2>>$OUTPUT_FILE &
    echo "task has been started!"
    ##=============================================
  else
    echo "the task is alreay running !!"
  fi 
elif [ "$1" = "debug" ] ; then
    echo "`date`:debug --------------------------------" >> $OUTPUT_FILE 
    nohup java $JAVA_OPTS $JAVA_DEBUG_OPT -cp $CLASSPATH com.suning.app.spy.core.Agent 1>>$OUTPUT_FILE 2>>$OUTPUT_FILE &
    echo "task has been debug!"
else
  echo "usage: $0 [start|debug]"
fi
