# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
#class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontobfuscate
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable

-keepattributes LineNumberTable,SourceFile

# Keep activity aliases from getting obfuscated
# Will throw IllegalArgumentException otherwise with reason "component class not exist"

-keep class app.simple.positional.activities.alias.IconOneAlias
-keep class app.simple.positional.activities.alias.IconTwoAlias
-keep class app.simple.positional.activities.alias.IconThreeAlias
-keep class app.simple.positional.activities.alias.IconFourAlias
-keep class app.simple.positional.activities.alias.IconFiveAlias
-keep class app.simple.positional.activities.alias.IconSixAlias
-keep class app.simple.positional.activities.alias.IconSevenAlias
-keep class app.simple.positional.activities.alias.IconEightAlias
-keep class app.simple.positional.activities.alias.IconNineAlias
-keep class app.simple.positional.activities.alias.IconLegacyAlias
-keep class kotlin.KotlinNullPointerException