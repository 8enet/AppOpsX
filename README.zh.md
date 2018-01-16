## AppOpsX
[![Build Status](https://img.shields.io/travis/8enet/AppOpsX.svg)][1]
[![Release Version](https://img.shields.io/github/release/8enet/AppOpsX.svg)][2]
[![Issues](https://img.shields.io/github/issues/8enet/AppOpsX.svg)][3]
[![Software License](https://img.shields.io/github/license/8enet/AppOpsX.svg)][4]
[![Crowdin](https://d322cqt584bo4o.cloudfront.net/appopsx/localized.svg)][5]
[![coolapk](https://img.shields.io/badge/coolapk-download-blue.svg)][6]
[![preview version](https://img.shields.io/badge/preview%20version-download-orange.svg)][8]

一个使用appops的GUI权限管理器，能方便的控制权限，避免权限滥用。

<a href='https://play.google.com/store/apps/details?id=com.zzzmode.appopsx'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png' width='150'/></a>



### 特点:
* 快速应用搜索和排序
* 按权限分组列表
* 导出/恢复配置数据
* 自动忽略权限模板设置
* 多用户支持
* WiFi/移动数据联网控制
* 支持ROOT/ADB两种使用模式
* ...

### 使用方法:
#### Build:
    ./gradlew build
    
#### 自动关闭权限:
 * 开启此选项，当新安装应用后，会自动关闭模板中选择的权限，模板可以手动选择。

#### 使用ADB模式:
  * 部分支持网络ADB调试手机，在开发者选项中开启，选择端口为5555即可。
  * 对于不支持网络ADB调试的手机，将手机连接电脑，推荐使用命令方式启动
  `adb shell sh /sdcard/Android/data/com.zzzmode.appopsx/opsx.sh`


### 翻译
可以通过 [Crowdin][5] 帮助此项目提供更多语言的翻译。

#### BUG反馈与建议
请提交 [issues][3] 或者发送邮件To: 
[zlcn2200@yeah.net][7]

### 贡献
欢迎提供任何代码,设计,文案,功能特点等的内容。

### License
[MIT License][4]

[1]: https://travis-ci.org/8enet/AppOpsX
[2]: https://github.com/8enet/AppOpsX/releases
[3]: https://github.com/8enet/AppOpsX/issues
[4]: https://github.com/8enet/AppOpsX/blob/master/LICENSE
[5]: https://crowdin.com/project/appopsx
[6]: http://www.coolapk.com/apk/com.zzzmode.appopsx
[7]: mailto:zlcn2200@yeah.net
[8]: https://www.zzzmode.com/AppOpsX/apk/
