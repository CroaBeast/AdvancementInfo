package me.croabeast.lib.advancement;

import org.bukkit.advancement.Advancement;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Represents information about a Minecraft advancement.
 * This interface provides methods to access various properties and display details of an advancement.
 */
public interface AdvancementInfo {

    /**
     * Gets the Bukkit Advancement object associated with this advancement info.
     *
     * @return the Bukkit Advancement object.
     */
    @NotNull
    Advancement getBukkit();

    @Nullable
    Advancement getParent();

    /**
     * Gets the title of the advancement.
     *
     * @return the title of the advancement.
     */
    @NotNull
    String getTitle();

    /**
     * Gets the description of the advancement.
     *
     * @return the description of the advancement.
     */
    @NotNull
    String getDescription();

    /**
     * Converts the description of the advancement into an array of strings, each of the specified length.
     * This method tokenizes the description text, ensuring that each line does not exceed the specified length.
     *
     * @param length the maximum length of each line in the array.
     * @return an array of strings, each representing a line of the description.
     */
    @NotNull
    default String[] getDescriptionArray(int length) {
        final String desc = getDescription();

        StringTokenizer tok = new StringTokenizer(desc, " ");
        StringBuilder out = new StringBuilder(desc.length());

        int lineLen = 0;
        final String split = Pattern.quote("\n");

        while (tok.hasMoreTokens()) {
            String word = tok.nextToken();
            int i = length - lineLen;

            while (word.length() > length) {
                out.append(word, 0, i).append(split);
                word = word.substring(i);
                lineLen = 0;
            }

            if (lineLen + word.length() > length) {
                out.append(split);
                lineLen = 0;
            }

            out.append(word).append(" ");
            lineLen += word.length() + 1;
        }

        return out.toString()
                .replaceAll("\\\\[QE]", "").split(split);
    }

    /**
     * Gets the icon of the advancement.
     *
     * @return the icon of the advancement, or null if there is no icon.
     */
    @Nullable
    ItemStack getIcon();

    /**
     * Checks if the advancement shows a toast notification.
     *
     * @return true if the advancement shows a toast notification, false otherwise.
     */
    boolean doesShowToast();

    /**
     * Checks if the advancement is hidden.
     *
     * @return true if the advancement is hidden, false otherwise.
     */
    boolean isHidden();

    /**
     * Checks if the advancement announces its completion to chat.
     *
     * @return true if the advancement announces to chat, false otherwise.
     */
    boolean doesAnnounceToChat();

    /**
     * Gets the X coordinate of the advancement's display position.
     *
     * @return the X coordinate.
     */
    float getX();

    /**
     * Gets the Y coordinate of the advancement's display position.
     *
     * @return the Y coordinate.
     */
    float getY();

    /**
     * Gets the frame type of the advancement.
     *
     * @return the frame type.
     */
    @NotNull
    Frame getFrame();

    /**
     * Gets the criteria map of the advancement.
     *
     * @return a map representing the criteria of the advancement.
     */
    @NotNull
    Map<String, Object> getCriteria();

    /**
     * Gets the rewards associated with the advancement.
     *
     * @return an object representing the rewards, or null if there are no rewards.
     */
    @Nullable
    Object getRewards();

    /**
     * Gets the requirements of the advancement as a 2D list of strings.
     *
     * @return a 2D list representing the requirements, or null if there are no requirements.
     */
    @Nullable
    List<List<String>> getRequirements();

    /**
     * Creates an AdvancementInfo instance from a given Bukkit Advancement object.
     * This method determines the appropriate implementation based on the server and version.
     *
     * @param advancement the Bukkit Advancement object.
     * @return an AdvancementInfo instance, or null if an error occurs.
     */
    @Nullable
    static AdvancementInfo from(@NotNull Advancement advancement) {
        try {
            if (ReflectionUtils.MC_VS >= 17.1)
                try {
                    return new PaperInfoImpl(advancement);
                } catch (Exception e) {
                    if (ReflectionUtils.MC_VS >= 18)
                        return new BukkitInfoImpl(advancement);
                }

            return new ReflectInfoImpl(advancement);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Enumeration representing the frame types of an advancement.
     */
    enum Frame {
        /**
         * Represents an unknown frame type. This is used as a fallback when the frame type cannot be determined.
         */
        UNKNOWN,

        /**
         * Represents a task frame type. Tasks are basic objectives that can be completed by players.
         */
        TASK,

        /**
         * Represents a goal frame type. Goals are intermediate objectives that are more challenging than tasks.
         */
        GOAL,

        /**
         * Represents a challenge frame type. Challenges are the most difficult objectives to achieve.
         */
        CHALLENGE;

        /**
         * Converts a string to a corresponding Frame enum.
         * If the string is null or empty, or does not match any frame type, UNKNOWN is returned.
         *
         * @param name the string representing the frame type.
         * @return the corresponding Frame enum, or UNKNOWN if the string is invalid.
         */
        public static Frame from(@Nullable String name) {
            if (name == null || name.isEmpty())
                return UNKNOWN;

            name = name.toUpperCase(Locale.ENGLISH);

            for (Frame type : values())
                if (name.equals(type.name())) return type;

            return UNKNOWN;
        }
    }
}
