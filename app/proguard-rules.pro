# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\android\sdk/tools/proguard/proguard-android.txt
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

# ------------------------------
# Общие правила Android
# ------------------------------
# Сохраняем классы, необходимые Android‑runtime
-keep class * extends android.app.Activity
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider
-keep class * extends android.view.View
-keepclassmembers class * {
    public <init>(android.content.Context);
    public void <init>(android.content.Context, android.util.AttributeSet);
    public void <init>(android.content.Context, android.util.AttributeSet, int);
}

# ------------------------------
# AndroidX (Fragment, AppCompat, Preference)
# ------------------------------
# Фрагменты и их менеджеры
-keep class androidx.fragment.app.Fragment { *; }
-keep class androidx.fragment.app.FragmentActivity { *; }
-keep class androidx.fragment.app.FragmentManager { *; }

# AppCompat (Activity, Toolbar, AppCompatDelegate и т.д.)
-keep class androidx.appcompat.** { *; }

# Preference‑библиотека
-keep class androidx.preference.** { *; }

# ------------------------------
# Kotlin‑стандартная библиотека
# ------------------------------
-keep class kotlin.** { *; }
-keepclassmembers class kotlin.Metadata { *; }

# ------------------------------
# Библиотека exp4j (математический парсер)
# ------------------------------
-keep class net.objecthunter.exp4j.** { *; }

# ------------------------------
# Сохранить имена методов, вызываемых рефлексивно (если есть)
# ------------------------------
# Пример: если в коде есть вызов Class.forName("com.example.MyClass")
#-keep class com.example.MyClass { *; }

# ------------------------------
# Отключить обфускацию ресурсов (необязательно)
# ------------------------------
-keepclassmembers class ** {
    @androidx.annotation.Keep *;
}

