#!/bin/sh
export JAVA_HOME=${spy_javahome}
export PATH=$JAVA_HOME/bin:$PATH

BASE="`dirname $0`"
LIB_PATH="$BASE/../lib"
LIB_EXT_PATH="$BASE/../lib_ext"
LIB_CONF="$BASE/../conf"
LOCALCLASSPATH=`echo $LIB_PATH/*.jar | tr ' ' ':'`
export CLASSPATH=$LIB_CONF:$LOCALCLASSPATH

JAVA_DEBUG_OPT=" -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=${debug_port},server=y,suspend=y "
TASK_OPTION="-DtaskName=$TASK_NAME "
TIGER_JMX_OPT=" -Dcom.sun.management.config.file=$BASE/../conf/jmx_monitor_management.properties "

chmod 600 $BASE/../conf/*.properties

if [ "${spy_production}" = "true" ] ; then
	str=`file $JAVA_HOME/bin/java | grep 64-bit`
    if [ -n "$str" ]; then
        JAVA_MEM_OPTS=" -server -Xmx128m -Xms128m -Xmn96m -XX:PermSize=16m -XX:MaxPermSize=32m -Xss256k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:+UseCompressedOops"
    else
        JAVA_MEM_OPTS=" -server -Xms128m -Xmx128m -XX:NewSize=64m -XX:MaxNewSize=64m -XX:MaxPermSize=32m "
    fi
    ##productction can't debug
    JAVA_DEBUG_OPT=" "
    JAVA_OPTS=" $JAVA_MEM_OPTS $TASK_OPTION $TIGER_JMX_OPT"
else
	JAVA_MEM_OPTS=" -Xms1024m -Xmx1024m "
	JAVA_OPTS=" $JAVA_MEM_OPTS $TASK_OPTION $TIGER_JMX_OPT"
fi

function exit_root () {
    echo
    echo "ERROR! root (the superuser) can't run this script."
    echo
    exit 1
}

#if [ `id -u` = 0 ]
#then
#    exit_root
#fi