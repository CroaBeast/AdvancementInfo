# AdvancementInfo [1.12 - 1.20]

This class will help you to get the title, the description, and the frame type of advancement.
If you are using PaperMC as the base of your project, use its method instead of this.

# How to use it in your project?
	
1. Add this class to your project by copying it or adding it via Maven or Gradle.
2. Call a new instance for it:
```
Advancement adv = Bukkit.getAdvancement(key);
AdvancementInfo info = AdvancementInfo.from(adv);
```
3. Get the desired object(s):
```
String frame = info.getFrame().toString();
String title = info.getTitle();

String description = info.getDescription();

String[] descArray = info.getDescriptionArray(24);
ItemStack item = info.getItem();
```
4. Use it in whatever you want. Check the [ExampleClass](https://github.com/CroaBeast/AdvancementInfo/blob/main/Example.java) here.
5. Example Output: 

![](https://i.imgur.com/htglb6H.png)

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
        <version>c9d33fd7e7</version>
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
    implementation 'com.github.CroaBeast:AdvancementInfo:c9d33fd7e7'
}
```
