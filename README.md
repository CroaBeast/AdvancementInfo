# AdvancementInfo [1.12 - 1.18]

This class will help you to get the title, the description and the frame type of advancement.
If you are using PaperMC as the base of your project, use its method instead of this.

#	How to use it in your project?
	
1. Add this class into your project.
2. Call a new instance for it:
```
Advancement adv = Bukkit.getAdvancement(key);
AdvancementInfo info = new AdvancementInfo(adv);
```
3. Get the desired string(s):
```
String frameType = info.getFrameType();
String title = info.getTitle();

String description = info.getDescription();

String[] descArray = info.getDescription(24);
ItemStack item = info.getItem();
```
4. Use it in whatever you want. Check the ExampleClass here.
5. Output: ![alt text](https://i.imgur.com/XE0rwN7.png)
