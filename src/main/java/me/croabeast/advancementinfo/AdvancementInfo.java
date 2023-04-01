package me.croabeast.advancementinfo;

import org.bukkit.advancement.*;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * The class to get all the advancement info.
 * @author CroaBeast
 * @since 1.0
 */
public class AdvancementInfo extends NMSHandler {

    private String title = null, desc = null, frameType = null,
            toChat = null, hidden = null, parent = null;

    private String[][] requirements = null;

    private ItemStack item = null;
    private Object rewards = null, criteria = null;

    /**
     * The basic constructor of the class.
     * @param adv the required advancement
     */
    public AdvancementInfo(@NotNull Advancement adv) {
        Class<?> craftClass = getBukkitClass("advancement.CraftAdvancement");
        if (craftClass == null) return;

        Object nmsAdv = getObject(craftClass, craftClass.cast(adv), "getHandle");
        Object display = getObject(nmsAdv, is_19_4() ? "d" : "c");
        if (display == null) return;

        Object rawTitle = getObject(display, "a"), rawDesc = getObject(display, "b");

        Object title = null, description = null;
        if (rawTitle != null && rawDesc != null) {
            Class<?> chatClass = getVersion() >= 17 ?
                    getNMSClass("net.minecraft.network.chat", "IChatBaseComponent", false) :
                    getNMSClass(null, "IChatBaseComponent", true);

            if (chatClass != null) {
                String method = getVersion() < 13 ? "toPlainText" : "getString";
                title = getObject(chatClass, rawTitle, method);
                description = getObject(chatClass, rawDesc, method);
            }
        }

        Field itemField = null;
        try {
            itemField = display.getClass().getDeclaredField("c");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Object nmsItemStack = null;
        if (itemField != null) {
            itemField.setAccessible(true);
            try {
                nmsItemStack = itemField.get(display);
            } catch (Exception e) {
                e.printStackTrace();
            }
            itemField.setAccessible(false);
        }

        this.title = checkValue(title);
        desc = checkValue(description);
        frameType = checkValue(getObject(display, "e"), "PROGRESS");

        parent = checkValue(getObject(nmsAdv, "b", "getName"), "null");
        toChat = checkValue(getObject(display, "i"));
        hidden = checkValue(getObject(display, "j"));

        requirements = (String[][]) getObject(nmsAdv, is_19_4() ? "j" : "i");
        rewards = getObject(nmsAdv, is_19_4() ? "e" : "d");
        item = getBukkitItem(nmsItemStack);
        criteria = getObject(nmsAdv, is_19_4() ? "g" : "f");
    }

    private static boolean is_19_4() {
        return getVersion() >= 19.4;
    }

    /**
     * Get the advancement type. Can be {@link FrameType#TASK TASK}, {@link FrameType#GOAL GOAL},
     * {@link FrameType#CHALLENGE CHALLENGE} or {@link FrameType#UNKNOWN UNKNOWN} (if null).
     *
     * @return the type
     */
    @NotNull
    public String getFrameType() {
        return FrameType.getFrameType(frameType) + "";
    }

    /**
     * Gets the advancement title or main name.
     *
     * @return the title, can be null
     */
    @Nullable
    public String getTitle() {
        return title;
    }

    private String removeLiteralChars(String input) {
        return input.replaceAll("\\\\Q", "").replaceAll("\\\\E", "");
    }

    /**
     * Gets the description. If null, it will return "No description."
     *
     * @return the description
     */
    @NotNull
    public String getDescription() {
        return desc == null ? "No description." : desc.replaceAll("\\n", " ");
    }

    /**
     * Gets the description stripped into substrings with the input length.
     *
     * @param length a char length
     * @return the stripped description array
     */
    @NotNull
    public String[] getDescriptionArray(int length) {
        StringTokenizer tok = new StringTokenizer(getDescription(), " ");
        StringBuilder output = new StringBuilder(getDescription().length());

        int lineLen = 0;
        String delimiter = Pattern.quote("\n");

        while (tok.hasMoreTokens()) {
            String word = tok.nextToken();
            int i = length - lineLen;

            while (word.length() > length) {
                output.append(word, 0, i).append(delimiter);
                word = word.substring(i);
                lineLen = 0;
            }

            if (lineLen + word.length() > length) {
                output.append(delimiter);
                lineLen = 0;
            }

            output.append(word).append(" ");
            lineLen += word.length() + 1;
        }

        return removeLiteralChars(output.toString()).split(delimiter);
    }

    /**
     * Gets the name of the parent advancement
     *
     * @return the parent name
     */
    @NotNull
    public String getParent() {
        return parent;
    }

    /**
     * Checks if the advancement can be announced into the chat
     *
     * @return can announce to chat
     */
    public boolean announceToChat() {
        return Boolean.parseBoolean(toChat);
    }

    /**
     * Checks if the advancement is hidden.
     *
     * @return is hidden
     */
    public boolean isHidden() {
        return Boolean.parseBoolean(hidden);
    }

    /**
     * Gets the item that represents the advancement.
     *
     * @return the item, can be null
     */
    @Nullable
    public ItemStack getItem() {
        return item;
    }

    /**
     * Gets the rewards object, you should cast it with the NMS AdvancementRewards class.
     *
     * @return the rewards object, can be null
     */
    @Nullable
    public Object getRewards() {
        return rewards;
    }

    /**
     * Gets the criteria object. You SHOULD convert this object to a Map<String, Criterion> object.
     *
     * @return the criteria object, can be null
     */
    @Nullable
    public Object getCriteria() {
        return criteria;
    }

    /**
     * Get the String matrix object of the requirements.
     *
     * @return requirements, can be null
     */
    @Nullable
    public String[][] getRequirements() {
        return requirements;
    }
}
