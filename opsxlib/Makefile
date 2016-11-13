buildJar:
	gradle build
	dx --dex --output=build/appopsx.jar build/appopsx-orig.jar
	cp build/appopsx.jar ../app/src/main/assets
	cp appopsx ../app/src/main/assets

push:
	adb push appopsx /data/local/tmp/appopsx
	adb push build/appopsx.jar /data/local/tmp/appopsx.jar

run:
	adb shell 'chmod 777 /data/local/tmp/appopsx'
	adb shell 'chmod 777 /data/local/tmp/appopsx.jar'
	adb shell 'su -C /data/local/tmp/appopsx'

all: buildJar push run