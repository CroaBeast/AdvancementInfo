package me.croabeast.advancementinfo;

import com.google.common.collect.Sets;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * The enum class for the frame types.
 * @author CroaBeast
 * @since 1.0
 */
public enum FrameType {
    /**
     * The unknown type, when the input is null or not found.
     */
    UNKNOWN,
    /**
     * The basic and common advancement type.
     */
    TASK,
    /**
     * The goal type, used to achieve rare advancements.
     */
    GOAL,
    /**
     * The challenge type, for very-hard-to-get advancements.
     */
    CHALLENGE;

    /**
     * All the enum values in a HashSet.
     */
    public static final Set<FrameType> VALUES = Sets.newHashSet(values());

    /**
     * Parses the enum to its simple name in lowercase.
     * @return the enum name
     */
    @Override
    public String toString() {
        return name().toLowerCase();
    }

    /**
     * Gets the frame type from a string line.
     * @param name an input name
     * @return the respective frame type
     */
    public static FrameType getFrameType(@Nullable String name) {
        if (name == null) return UNKNOWN;
        for (FrameType type : VALUES) {
            if (name.toLowerCase().equals(type + "")) return type;
        }
        return UNKNOWN;
    }
}
