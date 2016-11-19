buildJar:
	gradle build
	dx --dex --output=build/appopsx.jar build/appopsx-orig.jar
	cp build/appopsx-common.jar ../opsxpro/libs
	cp build/appopsx.jar ../opsxpro/src/main/assets
	# cp appopsx ../opsxpro/src/main/assets

push:
	adb push appopsx /data/local/tmp/appopsx
	adb push build/appopsx.jar /data/local/tmp/appopsx.jar

run:
	adb shell 'chmod 777 /data/local/tmp/appopsx'
	adb shell 'chmod 777 /data/local/tmp/appopsx.jar'
	adb shell 'su -C /data/local/tmp/appopsx'

all: buildJar push run