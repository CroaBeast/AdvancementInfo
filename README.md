<p align="center">
    <a href="https://discord.com/invite/gzzhVqgy3b" alt="Support Server">
        <img alt="Discord" src="https://img.shields.io/discord/826555143398752286?style=for-the-badge&logo=discord&label=Support%20Server&color=635aea">
    </a>
</p>

# AdvancementInfo

AdvancementInfo API is a specialized library designed to simplify the retrieval and manipulation of Minecraft advancements in your plugins. It abstracts the underlying complexity of accessing advancement data—such as titles, descriptions, icons, criteria, rewards, and display settings—by providing a uniform interface and several concrete implementations that use reflection to work with Bukkit, Paper, and other server internals.

---

## Overview

The API is centered around the `AdvancementInfo` interface, which defines methods for obtaining detailed information about an advancement. Implementations like `BukkitInfoImpl`, `ReflectInfoImpl`, and `PaperInfoImpl` extract data from a Bukkit `Advancement` object using reflection and provide:

- **Advancement Details:** Title, description, icon, and display options.
- **Display Coordinates:** X and Y positions for advancement display.
- **Visual Frame:** The advancement’s frame type (e.g., TASK, GOAL, CHALLENGE).
- **Criteria and Requirements:** Data that defines how the advancement is achieved.
- **Rewards:** Information on the rewards granted upon completion.

---

## Key Features

- **Unified Interface:**  
  The `AdvancementInfo` interface provides a common API to access advancement data, regardless of the underlying implementation.

- **Multiple Implementations:**  
  Different implementations adapt to various server versions and environments:
  - **BukkitInfoImpl:** Uses Bukkit’s standard advancement display methods.
  - **ReflectInfoImpl:** Leverages reflection to extract advanced details.
  - **PaperInfoImpl:** Tailored for Paper servers, using Paper-specific methods and the LegacyComponentSerializer for text conversion.

- **Version Compatibility:**  
  The API intelligently selects the correct implementation based on the server’s Minecraft version, ensuring broad compatibility.

- **Reflection Based:**  
  The use of reflection enables the API to access internal fields and methods of advancements, providing rich information even when not directly exposed by the Bukkit API.

---

## Usage Example

Below is an example of how to use the AdvancementInfo API to retrieve and display information about a Minecraft advancement.

### Example: Retrieving Advancement Information

```java
package com.example.myplugin;

import me.croabeast.advancement.AdvancementInfo;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Retrieve a Bukkit advancement by its key
        Advancement advancement = Bukkit.getAdvancement(new org.bukkit.NamespacedKey(this, "example_advancement"));

        if (advancement != null) {
            // Create an AdvancementInfo instance from the advancement using the static factory method
            AdvancementInfo info = AdvancementInfo.create(advancement);

            if (info != null) {
                // Print advancement details to the console
                getLogger().info("Advancement Title: " + info.getTitle());
                getLogger().info("Description: " + info.getDescription());

                // Retrieve formatted description array with a max line length of 40 characters
                String[] lines = info.getDescriptionArray(40);
                for (String line : lines) {
                    getLogger().info("Line: " + line);
                }
            } else {
                getLogger().warning("Failed to retrieve advancement info.");
            }
        } else {
            getLogger().warning("Advancement not found.");
        }
    }
}
```

### Explanation

- **Retrieving an Advancement:**  
  The example obtains a Bukkit `Advancement` via its key.

- **Using the Factory Method:**  
  `AdvancementInfo.from(advancement)` returns an appropriate implementation (Bukkit, Paper, or Reflect) based on the server version.

- **Accessing Advancement Data:**  
  Once the `AdvancementInfo` instance is obtained, you can access its title, description, criteria, rewards, and display settings.

- **Formatting Description:**  
  The example demonstrates how to split the description into an array of lines, ensuring that no line exceeds a specified length.

---

## Maven / Gradle Installation

To include AdvancementInfo to the project, add the following repository and dependency to your build configuration. Replace `${version}` with the desired version tag.

### Maven

Add the repository and dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>croabeast-repo</id>
        <url>https://croabeast.github.io/repo/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>me.croabeast</groupId>
        <artifactId>AdvancementInfo</artifactId>
        <version>${version}</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

### Gradle

Add the repository and dependency to your `build.gradle`:

```groovy
repositories {
    maven {
        url "https://croabeast.github.io/repo/"
    }
}

dependencies {
    implementation "me.croabeast:AdvancementInfo:${version}"
}
```

Replace `${version}` with the appropriate module version.

---

## Conclusion

The AdvancementInfo API provides a powerful, unified approach to accessing and processing Minecraft advancements. It abstracts away the complexities of reflection and server version differences, allowing you to focus on using the advancement data in your plugin. Whether you are building custom advancement displays, integrating advancement data into your plugin logic, or simply logging advancement details, this API makes it easier to work with advancements in a consistent manner.

Happy coding!  
— *CroaBeast*
