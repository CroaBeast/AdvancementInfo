package me.croabeast.advancement;

import org.bukkit.advancement.Advancement;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Represents information about a Minecraft advancement.
 * <p>
 * The {@code AdvancementInfo} interface provides methods to access various properties of an advancement,
 * such as its Bukkit representation, parent advancement, title, description, icon, display options, coordinates,
 * frame type, criteria, rewards, and requirements. It also includes a default helper method to format the
 * description into an array of strings with a specified maximum line length.
 * </p>
 * <p>
 * A static factory method {@link #create(Advancement)} is provided to generate an instance of {@code AdvancementInfo}
 * from a given Bukkit {@link Advancement}. The implementation returned may vary depending on the Minecraft
 * server version.
 * </p>
 *
 * @see Advancement
 * @see ItemStack
 */
public interface AdvancementInfo {

    /**
     * Returns the Bukkit {@link Advancement} object that represents this advancement.
     *
     * @return the Bukkit advancement.
     */
    @NotNull
    Advancement getBukkit();

    /**
     * Returns the parent advancement of this advancement, if one exists.
     *
     * @return the parent {@link AdvancementInfo}, or {@code null} if this advancement has no parent.
     */
    @Nullable
    Advancement getParent();

    /**
     * Returns the title of this advancement.
     *
     * @return the title as a {@link String}.
     */
    @NotNull
    String getTitle();

    /**
     * Returns the description of this advancement.
     *
     * @return the description as a {@link String}.
     */
    @NotNull
    String getDescription();

    /**
     * Returns the description of this advancement formatted as an array of strings.
     * <p>
     * The description is tokenized and split into multiple lines such that no line exceeds the specified length.
     * Newlines are inserted where appropriate.
     * </p>
     *
     * @param length the maximum length for each line.
     * @return an array of {@link String} representing the formatted description.
     */
    @NotNull
    default String[] getDescriptionArray(int length) {
        final String desc = getDescription();

        StringTokenizer token = new StringTokenizer(desc, " ");
        StringBuilder out = new StringBuilder(desc.length());

        int lineLen = 0;
        final String split = Pattern.quote("\n");

        while (token.hasMoreTokens()) {
            String word = token.nextToken();
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

        return out.toString().replaceAll("\\\\[QE]", "").split(split);
    }

    /**
     * Returns the icon of this advancement.
     *
     * @return the {@link ItemStack} representing the advancement's icon, or {@code null} if none is defined.
     */
    @Nullable
    ItemStack getIcon();

    /**
     * Indicates whether this advancement should display a toast notification.
     *
     * @return {@code true} if a toast is shown; {@code false} otherwise.
     */
    boolean doesShowToast();

    /**
     * Indicates whether this advancement is hidden.
     *
     * @return {@code true} if the advancement is hidden; {@code false} otherwise.
     */
    boolean isHidden();

    /**
     * Indicates whether this advancement's completion is announced in chat.
     *
     * @return {@code true} if the advancement is announced in chat; {@code false} otherwise.
     */
    boolean doesAnnounceToChat();

    /**
     * Returns the x-coordinate of this advancement's display position.
     *
     * @return the x-coordinate as a float.
     */
    float getX();

    /**
     * Returns the y-coordinate of this advancement's display position.
     *
     * @return the y-coordinate as a float.
     */
    float getY();

    /**
     * Returns the frame type of this advancement.
     * <p>
     * The frame type indicates the visual style of the advancement (e.g., task, goal, challenge).
     * </p>
     *
     * @return the {@link Frame} of the advancement.
     */
    @NotNull
    Frame getFrame();

    /**
     * Returns the criteria required to achieve this advancement.
     * <p>
     * The criteria are represented as a map of criterion names to their corresponding conditions.
     * </p>
     *
     * @return a map of criterion names to objects representing the conditions (never {@code null}).
     */
    @NotNull
    Map<String, Object> getCriteria();

    /**
     * Returns the rewards granted upon completing this advancement.
     *
     * @return an object representing the rewards, or {@code null} if none are defined.
     */
    @Nullable
    Object getRewards();

    /**
     * Returns the requirements for this advancement.
     * <p>
     * The requirements are defined as a list of lists, where each inner list represents a set of criterion names
     * that must be met.
     * </p>
     *
     * @return a list of lists of strings representing the requirements, or {@code null} if not defined.
     */
    @Nullable
    List<List<String>> getRequirements();

    /**
     * Creates an {@code AdvancementInfo} instance from a given Bukkit {@link Advancement}.
     * <p>
     * This static method attempts to create an {@code AdvancementInfo} using different implementation strategies
     * based on the server version (e.g., Paper or Bukkit). If none of the strategies work, it returns {@code null}.
     * </p>
     *
     * @param advancement the Bukkit {@link Advancement} to convert.
     * @return an {@code AdvancementInfo} instance representing the advancement, or {@code null} if creation fails.
     */
    @Nullable
    static AdvancementInfo create(@NotNull Advancement advancement) {
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
     * Enum representing the different frame types available for advancements.
     */
    enum Frame {
        /**
         * Represents an unknown frame type.
         */
        UNKNOWN,
        /**
         * Represents a task frame.
         */
        TASK,
        /**
         * Represents a goal frame.
         */
        GOAL,
        /**
         * Represents a challenge frame.
         */
        CHALLENGE;

        /**
         * Converts a string into its corresponding {@link Frame} type.
         * <p>
         * The conversion is case-insensitive. If the input is {@code null} or does not match any defined frame,
         * {@link Frame#UNKNOWN} is returned.
         * </p>
         *
         * @param name the frame name to convert.
         * @return the corresponding {@link Frame} type.
         */
        public static Frame fromName(@Nullable String name) {
            if (name == null || name.isEmpty())
                return UNKNOWN;

            name = name.toUpperCase(Locale.ENGLISH);
            for (Frame type : values())
                if (name.equals(type.name())) return type;

            return UNKNOWN;
        }
    }
}
