#!/system/bin/sh

function queryconfig(){
out=$(content query --uri content://com.zzzmode.appopsx.provider.local/$1)
local retvar=$1
if [[ $out == Row* ]] ; then
  value=${out##*=}
	echo "get "$1" "$value
	eval $retvar="'$value'"
else
	echo "get "$1" error"
	echo $out
	exit 1
fi	

}

queryconfig token

queryconfig classpath

arch=""
prop=$(getprop ro.product.cpu.abi)
if [[ $prop == arm64* ]] ; then
	arch="64"
fi

echo "device arch "$prop

type=""

if [[ $(id) == uid=0* ]] ; then
    queryconfig socketPath
    type="type:root,path:"$socketPath
else
    queryconfig adbPort
    type="type:adb,path:"$adbPort
fi

args=$type",bgrun:1,token:"$token

echo "AppOpsX args: "$args
export CLASSPATH=$classpath
app_process /system/bin com.zzzmode.appopsx.server.AppOpsMain "$args" >&2 &
echo $?
echo "\n\nUse Ctrl+C to exit. \n\n"
exit 0

