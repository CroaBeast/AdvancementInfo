# AdvancementInfo

This class will help you to get the title, the description and the frame type of an advancement.
Only works in 1.12 to 1.18.

#	How to use it in your project?
	
1. Add this class into your project.
2. Call a new instance for it:
```java
Advancement adv = Bukkit.getAdvancement(key);
AdvancementInfo info = new AdvancementInfo(adv);
```
3. Get the desired string:
```java
String frameType = info.getFrameType();
String title = info.getTitle();
String description = info.getDescription();
```
4. Use it in whatever you want.
```java
player.sendMessage("[" + getFrameType + "] " + title + ": " + description);
```
5. Output: ![alt text](https://i.imgur.com/XE0rwN7.png)
