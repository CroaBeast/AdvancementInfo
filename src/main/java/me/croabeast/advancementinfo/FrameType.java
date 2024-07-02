package me.croabeast.advancementinfo;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * An enum representing different types of advancement frames.
 */
public enum FrameType {
    /**
     * Unknown frame type.
     */
    UNKNOWN,
    /**
     * Task frame type.
     */
    TASK,
    /**
     * Goal frame type.
     */
    GOAL,
    /**
     * Challenge frame type.
     */
    CHALLENGE;

    /**
     * Retrieves the FrameType associated with the given name.
     *
     * @param name The name of the FrameType.
     * @return The FrameType associated with the name, or UNKNOWN if not found.
     */
    public static FrameType getFrameType(@Nullable String name) {
        if (name == null || name.isEmpty())
            return UNKNOWN;

        name = name.toUpperCase(Locale.ENGLISH);

        for (FrameType type : values()) {
            if (name.equals(type.name())) return type;
        }

        return UNKNOWN;
    }
}
