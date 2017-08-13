#!/usr/bin/env sh
#==================================
# configuration sector
#==================================

# 获取APP所在的目录的绝对路径
function get_abs_dir() {
    SOURCE="${BASH_SOURCE[0]}"
    # resolve $SOURCE until the file is no longer a symlink
    while [ -h "$SOURCE" ]; do
        TARGET="$(readlink "$SOURCE")"
        if [[ ${SOURCE} == /* ]]; then
            # echo "SOURCE '$SOURCE' is an absolute symlink to '$TARGET'"
            SOURCE="$TARGET"
        else
            DIR="$(dirname "$SOURCE")"
            # echo "SOURCE '$SOURCE' is a relative symlink to '$TARGET' (relative to '$DIR')"
            # if $SOURCE was a relative symlink, we need to resolve it
            # relative to the path where the symlink file was located
            SOURCE="$DIR/$TARGET"
        fi
    done
    # echo "SOURCE is '$SOURCE'"

    # RDIR="$( dirname "$SOURCE" )"
    DIR="$( cd -P "$( dirname "$SOURCE" )" && cd .. && pwd )"
    # if [ "$DIR" != "$RDIR" ]; then
    #     echo "DIR '$RDIR' resolves to '$DIR'"
    # fi
    # echo "DIR is '$DIR'"
    echo $DIR
}

APP_HOME=`get_abs_dir`
VERSION=1.0.0
APP_NAME=anysql
JAR=${APP_NAME}-${VERSION}.jar
MAIN_CLASS=com.asql.tools.ASQL

# *NOTE* 多网卡下不正确
IP="$( LC_ALL=C ifconfig  | grep 'inet addr:'| grep -v '127.0.0.1' |cut -d: -f2 | awk '{ print $1}' )"
COUNT=1
# 不同机器应配置不同的标识，建议用ip:counter形式表示。
APP_ID="${IP}:${COUNT}"
APP_CONF_DIR="${APP_DIR}/conf"
APP_ARGS=""


# Add default JVM options here. You can also use JAVA_OPTS to pass JVM options to this script.
JAVA_OPTS="${JAVA_OPTS} -server -Xms128M -Xmx512M -Xss256K \
      -XX:PermSize=96m -XX:MaxPermSize=128m \
      -XX:+DisableExplicitGC -XX:SurvivorRatio=1 \
      -XX:GCTimeRatio=19 -XX:+UseConcMarkSweepGC -XX:+UseParNewGC \
      -XX:+CMSParallelRemarkEnabled -XX:+CMSClassUnloadingEnabled \
      -XX:+UseCMSCompactAtFullCollection -XX:+UseFastAccessorMethods \
      -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=80 \
      -XX:SoftRefLRUPolicyMSPerMB=0  -XX:CMSFullGCsBeforeCompaction=1 \
      -XX:+AggressiveOpts -XX:+PrintGCDetails -XX:+PrintGCTimeStamps \
      -XX:+HeapDumpOnOutOfMemoryError -Xloggc:${APP_HOME}/logs/${APP_NAME}_gc.log \
      -Dapp.home=${APP_HOME} -Dapp.id=${APP_ID} -Djava.awt.headless=true"

#-Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.Log4jLogDelegateFactory \
#-Dlog4j.configurationFile=${APP_CONF_LOG4J_FILE} \
# -Dvertx.cwd=${APP_WEB}

#io.vertx.core.logging.SLF4JLogDelegateFactory
#log4j.configuration for log4j,
#log4j.configurationFile for log4j2 and log4j

OUT_FILE=${APP_HOME}/logs/${APP_NAME}-nuhuo.log
RUNNING_PID="${APP_HOME}"/RUNNING_PID

# set classpath
classpath_munge () {
    if ! echo $CLASSPATH | /bin/egrep -q "(^|:)$1($|:)" ; then
        if [ "$2" = "after" ] ; then
            CLASSPATH=$CLASSPATH:$1
        else
            CLASSPATH=$1:$CLASSPATH
        fi
    fi
}

#CLASSPATH=${APP_HOME}/conf:${APP_HOME}/${JAR}
#CLASSPATH=${CLASSPATH}:$(JARS=("$APP_HOME"/lib/*.jar); IFS=:; echo "${JARS[*]}")

classpath_munge ${APP_CONF_DIR}
classpath_munge ${APP_DIR}/*
classpath_munge ${APP_DIR}/lib/*

#==================================
# execute sector
#==================================

mkdir -p ${APP_HOME}/logs


# colors
red='\e[0;31m'
green='\e[0;32m'
yellow='\e[0;33m'
reset='\e[0m'

echoRed() { echo -e "${red}$1${reset}"; }
echoGreen() { echo -e "${green}$1${reset}"; }
echoYellow() { echo -e "${yellow}$1${reset}"; }

warn ( )
{
    echoYellow "$*"
}

die ( ) 
{
    echo
    echoRed "$*"
    echo
    exit 1
}

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME
Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Escape application args
save ( ) {
    for i do printf %s\\n "$i" | sed "s/'/'\\\\''/g;1s/^/'/;\$s/\$/' \\\\/" ; done
    echo " "
}

ACTION=$1
shift

# Check whether the application is running.
# The check is pretty simple: open a running pid file and check that the process
# is alive.
is_running() {
    # Check for running app
    if [ -f "${RUNNING_PID}" ]; then
        proc=$(cat ${RUNNING_PID});
        if /bin/ps --pid ${proc} 1>&2 >/dev/null;
        then
            return 0
        fi
    fi
    return 1
}

#启动方法
start()
{
    if is_running; then
        echoYellow "---------------The ${APP_NAME} is already running------------"
        return 0
    fi
    pushd ${APP_HOME} > /dev/null
    nohup "$JAVACMD" $JAVA_OPTS  $VERTX_OPTS -classpath "$CLASSPATH" $MAIN_CLASS $APP_ARGS $@> ${OUT_FILE} 2>&1 &
    echo $! > ${RUNNING_PID}
    popd > /dev/null

    if is_running; then
        echoGreen "--------------The ${APP_NAME} started----------------------"
        exit 0
    else
        echoRed "The ${SERVICE_NAME} has not started - check log"
        exit 3
    fi
}

#重启方法
restart()
{
    echo "Restarting ${APP_NAME} with ${JAR}"
	stop
	start
}

#停止方法
stop()
{
    echoYellow "Stopping ${APP_NAME} with ${JAR}"

    if is_running; then
        kill `cat ${RUNNING_PID}`
        i=5;

        while [[ running && i -gt 0 ]]
        do
            echo -n "$i "
            sleep 1;
            ((i--))
        done;
        if is_running; then
            kill -KILL `cat ${RUNNING_PID}`
        fi
        rm $RUNNING_PID
        echo -e "\n"
    fi
}

#查询运行状态方法
status() {
    if is_running; then
        echoGreen "${APP_NAME} is running"
    else
        echoRed "${APP_NAME} is either stopped or inaccessible"
    fi
}

case "$ACTION" in
    start)
        start
        ;;
    stop)
        if is_running; then
            stop
            exit 0
        else
            echoRed "${SERVICE_NAME} not running"
            exit 3
        fi
        ;;
    restart)
        stop
        start
        ;;
    status)
        status
        exit 0
        ;;
    *)
        printf 'Usage: %s {status|start|stop|restart}\n'
        exit 1
        ;;
esac





