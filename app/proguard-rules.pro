# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/zl/develop/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepattributes SourceFile,LineNumberTable

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}

-keep public class com.zzzmode.appopsx.R$*{
public static final int *;
}


-keep class com.zzzmode.** { *; }

-keep class com.umeng.** { *; }
-keep class a.a.a.** { *; }

-keep class android.support.v4.view.ViewPager$OnPageChangeListener {*;}
-keep class android.support.v4.view.ViewPager {*;}
-keep class android.support.v4.view.PagerAdapter {*;}
-keep class android.support.v7.widget.SearchView { *;}