# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.shockwave**

-keep class cn.pedant.SweetAlert** { *; }
-keep class cn.pedant.SweetAlert.Rotate3dAnimation
-keep class com.itextpdf.** { *; }
-keep class org.** { *; }
-keep class org.openxmlformats.** { *; }
-keep class org.openxmlformats.** { *; }
-keep class org.apache.** { *; }
-keep class org.apache.poi.** { *; }
-keep class org.apache.poi.xwpf.** { *; }
-keep class org.apache.poi.xwpf.model.** { *; }
-keep class org.apache.poi.xwpf.extractor.** { *; }
-keep class org.apache.poi.xwpf.usermodel.** { *; }
-keep class org.apache.poi.hwpf.** { *; }
-keep class org.apache.poi.hwpf.usermodel.** { *; }
-keep class org.apache.poi.hwpf.extractor.** { *; }
-keep class org.apache.poi.hwpf.model.** { *; }
-keep class org.apache.poi.hwpf.converter.** { *; }
-keep class org.apache.poi.hwpf.dev.** { *; }
-keep class org.apache.poi.hwpf.sprm.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-keep class com.aspose.cells.** { *; }
