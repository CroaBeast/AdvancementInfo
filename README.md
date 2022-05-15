# AdvancementInfo [1.12 - 1.18]

This class will help you to get the title, the description and the frame type of advancement.
If you are using PaperMC as the base of your project, use its method instead of this.

# How to use it in your project?
	
1. Add this class into your project copying it or add it via Maven or Gradle.
2. Call a new instance for it:
```
Advancement adv = Bukkit.getAdvancement(key);
AdvancementInfo info = new AdvancementInfo(adv);
```
3. Get the desired object(s):
```
String frameType = info.getFrameType();
String title = info.getTitle();

String description = info.getDescription();

String[] descArray = info.getDescription(24);
ItemStack item = info.getItem();
```
4. Use it in whatever you want. Check the [ExampleClass](https://github.com/CroaBeast/AdvancementInfo/blob/main/Example.java) here.

# Maven and Gradle Integration
Maven - add to pom.xml
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
```xml
<dependencies>
    <dependency>
        <groupId>com.github.CroaBeast</groupId>
        <artifactId>AdvancementInfo</artifactId>
        <!--Replace version with the latest release version-->
        <version>RELEASE_TAG</version>
    </dependency>
</dependencies>
```

Gradle - add to build.gradle
```
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```
```
dependencies {
    implementation 'com.github.CroaBeast:AdvancementInfo:RELEASE_TAG'
}
```
