# AppOpsX
[![Build Status](https://img.shields.io/travis/8enet/AppOpsX.svg)](https://travis-ci.org/8enet/AppOpsX)   [![Release Version](https://img.shields.io/github/release/8enet/AppOpsX.svg)](https://github.com/8enet/AppOpsX/releases) [![Issues](https://img.shields.io/github/issues/8enet/AppOpsX.svg)](https://github.com/8enet/AppOpsX/issues)
[![Software License](https://img.shields.io/github/license/8enet/AppOpsX.svg)](LICENSE)
[![Crowdin](https://d322cqt584bo4o.cloudfront.net/appopsx/localized.svg)](https://crowdin.com/project/appopsx)

<a href='https://play.google.com/store/apps/details?id=com.zzzmode.appopsx&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width='150'/></a>

[酷安](http://www.coolapk.com/apk/com.zzzmode.appopsx)

主要是解决防止国内流氓应用在Android6.0上不给权限就不让用的问题。   
也可以不root通过`adb shell appops set [--user <USER_ID>] <PACKAGE> <OP> <MODE>` 忽略授予的权限。

支持ADB模式，可以在开发者选项中打开[网络ADB调试]功能或者使用电脑`adb tcpip 5555` (也可以手动指定其他端口)后使用。

`AppOpsX` 现在没有调用appops命令的方式去设置，用的是以root权限启用jar后开放`LocalSocks`通讯的方式去代理修改权限，
理论上多条执行的时候要比调用命令快，在应用内使用感觉会比较明显，当然底层和appops命令实现一致。

用法:
```
./gradlew build
```
将会启动一个`LocalServerSocket`,供client调用。
前端连接到`LocalSocks` 后即可发送命令去类似代理执行一样执行`IAppOpsService`中的方法。

> 所有的设置关闭权限使用了`MODE_IGNORED`，不要使用`MODE_ERRORED`，否则会导致应用设置->应用->配置应用->应用权限崩溃。
如有问题请提issue。

## 翻译 
您可以通过 [Crowdin](https://crowdin.com/project/appopsx) 帮助此项目翻译多语言。


## License
[MIT License](https://github.com/8enet/AppOpsX/blob/master/LICENSE)
