package me.croabeast.advancementinfo;

import org.apache.commons.lang.WordUtils;
import org.bukkit.advancement.Advancement;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import static me.croabeast.advancementinfo.NMSHandler.*;

/**
 * The <code>AdvancementInfo</code> class manages all the NMS objects of an advancement,
 * like its title, description, frame type, etc.
 *
 * @author CroaBeast
 * @since 1.0
 */
public class AdvancementInfo {

    private final Advancement adv;

    private final String title, desc, frameType, toChat, hidden, parent;
    private final ItemStack item;

    private final Object rewards, criteria, requirements;

    private static final String COMP_CLASS = "IChatBaseComponent";

    /**
     * The basic constructor of the class.
     *
     * @param adv the required advancement
     * @throws IllegalStateException if it fields to catch the values
     */
    public AdvancementInfo(@NotNull Advancement adv) throws IllegalStateException {
        this.adv = adv;

        Class<?> craftClass = getBukkitClass("advancement.CraftAdvancement");
        if (craftClass == null)
            throw new IllegalStateException();

        Object nmsAdv = getObject(craftClass, craftClass.cast(adv), "getHandle");
        if (is_20_2()) nmsAdv = getObject(nmsAdv, "b");

        Object display = getObject(nmsAdv, is_19_4() ? "d" : "c");
        if (display == null)
            throw new IllegalStateException();

        if (is_20_2()) {
            Optional<?> o = ((Optional<?>) display);
            if (o.isPresent()) display = o.get();
        }

        Object rawTitle = getObject(display, "a"), rawDesc = getObject(display, "b");

        Object title = null, description = null;
        if (rawTitle != null && rawDesc != null) {
            Class<?> chatClass = getVersion() >= 17 ?
                    getNMSClass("net.minecraft.network.chat", COMP_CLASS, false) :
                    getNMSClass(null, COMP_CLASS, true);

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
            try {
                itemField.setAccessible(true);
                nmsItemStack = itemField.get(display);
                itemField.setAccessible(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.title = checkValue(title);
        desc = checkValue(description);
        frameType = checkValue(getObject(display, "e"), "PROGRESS");

        parent = checkValue(getObject(nmsAdv, "b", "getName"), "null");
        toChat = checkValue(getObject(display, "i"));
        hidden = checkValue(getObject(display, "j"));

        item = getBukkitItem(nmsItemStack);
        requirements = getObject(nmsAdv, is_19_4() ? "j" : "i");
        rewards = getObject(nmsAdv, is_19_4() ? "e" : "d");
        criteria = getObject(nmsAdv, is_19_4() ? "g" :
                (getVersion() < 18 ? "getCriteria" : "f"));
    }

    private static boolean is_19_4() {
        return getVersion() >= 19.4;
    }

    private static boolean is_20_2() {
        return getVersion() >= 20.2;
    }

    @NotNull
    public Advancement getBukkit() {
        return adv;
    }

    /**
     * Returns the advancement type. Can be {@link FrameType#TASK TASK}, {@link FrameType#GOAL GOAL},
     * {@link FrameType#CHALLENGE CHALLENGE} or {@link FrameType#UNKNOWN UNKNOWN} (if null).
     *
     * @return the type
     */
    @NotNull
    public String getFrameType() {
        return FrameType.getFrameType(frameType) + "";
    }

    /**
     * Returns the advancement title or main name.
     *
     * @return the title, can be null
     */
    @NotNull
    public String getTitle() {
        if (title == null) {
            String key = adv.getKey().toString();

            key = key.substring(key.lastIndexOf('/') + 1);
            key = key.replace('_', ' ');

            return WordUtils.capitalizeFully(key);
        }

        return title;
    }

    /**
     * Returns the description. If null, it will return "No description"
     *
     * @return the description
     */
    @NotNull
    public String getDescription() {
        return desc == null ? "No description" : desc.replaceAll("\\n", " ");
    }

    /**
     * Returns the description stripped into substrings with the input length.
     *
     * @param length a char length
     * @return the stripped description array
     */
    @NotNull
    public String[] getDescriptionArray(int length) {
        final String desc = getDescription();

        StringTokenizer tok = new StringTokenizer(desc, " ");
        StringBuilder output = new StringBuilder(desc.length());

        int lineLen = 0;
        final String split = Pattern.quote("\n");

        while (tok.hasMoreTokens()) {
            String word = tok.nextToken();
            int i = length - lineLen;

            while (word.length() > length) {
                output.append(word, 0, i).append(split);
                word = word.substring(i);
                lineLen = 0;
            }

            if (lineLen + word.length() > length) {
                output.append(split);
                lineLen = 0;
            }

            output.append(word).append(" ");
            lineLen += word.length() + 1;
        }

        return output.toString()
                .replaceAll("\\\\Q|\\\\E", "").split(split);
    }

    /**
     * Returns the name of the parent advancement
     *
     * @return the parent name
     */
    @NotNull
    public String getParent() {
        return parent;
    }

    private static boolean getBool(String string) {
        return string.matches("(?i)true|false") && string.matches("(?i)true");
    }

    /**
     * Returns if the advancement can be announced into the chat
     *
     * @return can announce to chat
     */
    public boolean announceToChat() {
        return getBool(toChat);
    }

    /**
     * Returns if the advancement is hidden.
     *
     * @return is hidden
     */
    public boolean isHidden() {
        return getBool(hidden);
    }

    /**
     * Returns the item that represents the advancement.
     *
     * @return the item, can be null
     */
    @Nullable
    public ItemStack getItem() {
        return item;
    }

    /**
     * Returns the rewards object, you should cast it with the AdvancementRewards NMS class.
     *
     * @return the rewards object, can be null
     */
    @Nullable
    public Object getRewards() {
        return rewards;
    }

    /**
     * Returns the criteria map that stores all the criteria of the advancement.
     * <p> The values should be cast back using the Criteria/Criterion NMS class.
     *
     * @return the criteria map, will return an empty map if there is no criteria
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public Map<String, Object> getCriteria() {
        try {
            return criteria == null ? new HashMap<>() : (Map<String, Object>) criteria;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * Returns the String matrix of the requirements.
     *
     * @return requirements, can be null
     */
    @Nullable
    public String[][] getRequirements() {
        try {
            return (String[][]) requirements;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "AdvancementInfo{title='" + title + "', frameType='" + frameType + "'}";
    }
}
