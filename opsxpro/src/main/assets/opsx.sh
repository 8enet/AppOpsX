#!/system/bin/sh

classpath=%s
args=%s

if [ "$classpath" == "$args" ]; then
    sh $(dirname $0)"/opsx-auto.sh"
    exit 0
fi

echo start

id
#export CLASSPATH=$classpath
#app_process /system/bin --nice-name=appopsx_local_server com.zzzmode.appopsx.server.AppOpsMain "$args" >&2 &

cp -f /sdcard/Android/data/com.zzzmode.appopsx/opsxstart /data/local/tmp/opsxstart

chmod 755 /data/local/tmp/opsxstart
chown shell:shell /data/local/tmp/opsxstart

echo "classpath --> $classpath"
echo "args --->  $args"
/data/local/tmp/opsxstart --env CLASSPATH=$classpath --args /system/bin/app_process /system/bin --nice-name=appopsx_local_server com.zzzmode.appopsx.server.AppOpsMain $args

ret=$?
if [ $ret -ne 0 ]; then
    echo "start error,return code  $ret \n"
else
    echo "start success."
    echo "\n\nUse Ctrl+C to exit. \n\n"
fi

ps |grep appopsx