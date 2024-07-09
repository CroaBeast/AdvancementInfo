package me.croabeast.lib.advancement;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.advancement.Advancement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
abstract class AdvancementImpl implements AdvancementInfo {

    private final Advancement bukkit;
    @Getter(AccessLevel.NONE)
    Object handle;

    @NotNull
    private final Map<String, Object> criteria;
    @Nullable
    private final Object rewards;
    @Nullable
    private final String[][] requirements;

    @SuppressWarnings("unchecked")
    AdvancementImpl(Advancement advancement) {
        bukkit = Objects.requireNonNull(advancement);

        Class<?> craft;
        try {
            craft = ReflectionUtils.fromCraftBukkit("advancement.CraftAdvancement");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        try {
            Method method = Objects.requireNonNull(craft).getMethod("getHandle");
            handle = method.invoke(craft.cast(advancement));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        Field criteriaField = null;
        Field rField = null;
        Field rewardsField = null;

        Field[] handleFields = handle.getClass().getDeclaredFields();

        Class<?> rewardsClass = ReflectionUtils.MC_VS >= 17.0 ?
                ReflectionUtils.from("net.minecraft.advancements.AdvancementRewards") :
                ReflectionUtils.getNmsClass("AdvancementRewards");

        for (Field field : handleFields) {
            final Class<?> fieldClass = field.getType();

            if (fieldClass == String[][].class) {
                rField = field;
                continue;
            }

            if (fieldClass == Map.class) {
                criteriaField = field;
                continue;
            }

            if (fieldClass == rewardsClass) rewardsField = field;
        }

        this.criteria = (Map<String, Object>)
                ReflectionUtils.fromField(criteriaField, handle, Map.class, new HashMap<>());

        this.rewards = ReflectionUtils.fromField(rewardsField, handle, Object.class, null);
        this.requirements = ReflectionUtils.fromField(rField, handle, String[][].class, null);
    }

    @Override
    public String toString() {
        return "AdvancementInfo{bukkit=" + bukkit.getKey() + '}';
    }
}
