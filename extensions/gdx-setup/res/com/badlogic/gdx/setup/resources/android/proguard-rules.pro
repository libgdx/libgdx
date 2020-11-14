
-verbose

# ignore external lib classes 
-keep class !%PACKAGE%.**,** { *; }

# keep enums
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# example to ignore classes or packages
#-keep class %PACKAGE%.screen.MainScreen { *; }
#-keep class %PACKAGE%.api.dto.** { *; }
