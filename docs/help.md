[获取更多信息](https://8enet.github.io/AppOpsX)

---
### 简介

1. **这个应用是干嘛的?** 
   * 忽略权限，避免部分应用强行申请权限
   * Android 7.0上通过忽略`RUN_IN_BACKGROUND`可以避免一直后台运行
   * 可以控制禁止网络访问(仅Root模式下可用)
2. **有哪些使用场景？**   
   * 想要干净的通知栏？关闭应用使用通知栏权限！
   * 不想让应用读取手机设备等信息？但是不给权限又不让进去使用怎么办？关闭读取手机状态和身份权限！
   * 想让某个应用处于静音状态？使用显示默认权限，然后关闭播放声音的权限！
   * 不想让应用联网？禁止WiFi/移动数据都可以！
   * 还有禁止读取通讯录/短信 等等更多使用场景。
3. **这个应用收费吗？**  
   本应用 **完全免费无广告!** 所有代码开源托管在 [https://github.com/8enet/AppOpsX][1]
   
4. **如何反馈问题？**
   * 可以在GitHub提交 [issues][2]
   * 在 [Google Play][3]，[酷安][4] 发表评论
   * 发送邮件到 [zlcn2200@yeah.net][5]

5. **我能为此应用做些什么？**  
   * 如果你喜欢本应用，可以在[Google Play][3]或者[酷安市场][4]
   给予★★★★★ 5星好评！欢迎评价！
   * 可以[帮助此项目翻译和补全更多种类的语言][6]
   * 如果有任何更好的意见，欢迎补充！

### 使用过程中的一些问题
1. **出现`java.lang.RuntimeException: connect fail!` 无法使用怎么办？**   
  首先请尝试开启设置中的【Root兼容模式】。   
  检测是否有出现请求Root权限的弹窗。   
  如果没有出现，请尝试退出应用后在SuperSU中默认允许AppOpsX授权然后重试。如果有出现Root授权弹窗后还是出现这个问题，请点击 AppOpsX设置->关闭后台服务->退出应用 后重试。   
  如果还是一直出现这个问题，请在打开 SuperSU->设置->日志记录方式->选为[全部内容]，然后再在AppOpsX中重试，最后请到SuperSU->日志中查看最近一条关于AppOpsX的日志，截图提交反馈，谢谢！

2. **不想Root手机，怎么使用ADB模式？**
   * 如果开发者选项中有[网络ADB调试]功能的，请直接打开此选项，然后在AppOpsX中选择使用ADB模式即可。
   * 没有[网络ADB调试]功能的，需要打开USB调试功能，连接电脑后在终端输入`adb tcpip 5555` 即可，如果选择使用其他端口，需要在AppOpsX中设置相应端口。
   * 直接使用脚本启动，从1.1.9版本开始支持脚本方式启动，开启USB调试连接电脑后使用 `adb shell sh /sdcard/Android/data/com.zzzmode.appopsx/opsx.sh &` 

3. **为什么有一些权限关闭后又自动打开了？**   
   有一些权限用appops改动并不会生效，所以再次查看的时候会发现又自动打开了。
   
4. **为什么我禁止了WiFi/网络访问，重启后又生效了？**  
   因为iptables重启失效了，解决方法是在应用配置完成后使用 **导出** 功能导出配置，下次重启后恢复权限配置即可。

5. **为什么自动关闭权限有时候在安装完成后没有弹出提示？**   
   应用被完全停止后收不到新安装广播，所以无法弹出提示，请根据实际情况手动添加白名单。

Last Edited on 2017-09-27

[1]: https://github.com/8enet/AppOpsX
[2]: https://github.com/8enet/AppOpsX/issues
[3]: https://play.google.com/store/apps/details?id=com.zzzmode.appopsx
[4]: https://www.coolapk.com/apk/com.zzzmode.appopsx
[5]: mailto:zlcn2200@yeah.net
[6]: https://crowdin.com/project/appopsx