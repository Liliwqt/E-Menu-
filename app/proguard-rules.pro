# Preserve line numbers while hiding source file names in release stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Firebase deserializes MenuItem reflectively. Room, Hilt, and kotlinx.serialization
# ship/generated their own targeted rules and do not need app-wide keep rules.
-keep class com.example.androidkiosk.model.MenuItem { *; }

# Release builds have no Timber tree; remove logging calls and their format strings.
-assumenosideeffects class timber.log.Timber {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** wtf(...);
}
