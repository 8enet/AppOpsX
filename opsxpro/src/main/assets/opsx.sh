#!/system/bin/sh

classpath=%s
args=%s

echo start
id
ls -l /sdcard/Android/data/com.zzzmode.appopsx/

ncls=/data/local/tmp/appopsx.jar
cp -f $classpath $ncls

ret=$?
if [ $ret -ne 0 ]; then
  classpath=/sdcard/Android/data/com.zzzmode.appopsx/appopsx.jar
  cp -f $classpath $ncls
fi

chmod 755 $ncls
chown shell:shell $ncls

echo "classpath --> $classpath"
echo "args --->  $args"


export CLASSPATH=$ncls
exec app_process /system/bin --nice-name=appopsx_local_server com.zzzmode.appopsx.server.AppOpsMain "$args"  &

ret=$?
if [ $ret -ne 0 ]; then
    echo "start error,return code  $ret \n"
else
    echo "start success."
    echo "\n\nUse Ctrl+C to exit. \n\n"
fi

ps |grep appopsx