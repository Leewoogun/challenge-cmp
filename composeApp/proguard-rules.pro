# 크래시 스택에 줄 번호를 남긴다. 클래스·메서드 이름은 mapping.txt 로 복원(retrace)한다.
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile

# Kakao SDK (공식 가이드). 내부 Retrofit 이 제네릭 시그니처를 리플렉션으로 읽는데,
# R8 full mode 가 이를 지우면 앱 시작 시 "Call return type must be parameterized" 로 죽는다.
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keep class com.kakao.sdk.**.model.* { <fields>; }
-keep interface com.kakao.sdk.**.*Api
-keep class * extends com.google.gson.TypeAdapter

-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.*
-dontwarn org.openjsse.**
