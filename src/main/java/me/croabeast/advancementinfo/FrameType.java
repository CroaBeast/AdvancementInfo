package me.croabeast.advancementinfo;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum FrameType {
    UNKNOWN,
    TASK,
    GOAL,
    CHALLENGE;

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }

    public static FrameType getFrameType(@Nullable String name) {
        if (StringUtils.isEmpty(name)) return UNKNOWN;

        name = name.toLowerCase(Locale.ENGLISH);

        for (FrameType type : values()) {
            if (name.equals(type + "")) return type;
        }

        return UNKNOWN;
    }
}
