package me.croabeast.lib.advancement;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;

@Getter
abstract class AdvancementImpl implements AdvancementInfo {

    private final Advancement bukkit;
    @Getter(AccessLevel.NONE)
    protected Object handle;

    private Advancement parent = null;

    @NotNull
    private Map<String, Object> criteria = new HashMap<>();
    @Nullable
    private Object rewards = null;
    @Nullable
    private List<List<String>> requirements = null;

    @SuppressWarnings("all")
    AdvancementImpl(Advancement advancement) {
        bukkit = Objects.requireNonNull(advancement);

        Class<?> craft;
        try {
            craft = ReflectionUtils.fromBukkit("advancement.CraftAdvancement");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        try {
            Method method = Objects.requireNonNull(craft).getMethod("getHandle");
            handle = method.invoke(craft.cast(advancement));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        ReflectionUtils.FieldFinder find = ReflectionUtils.from(handle);
        if (handle.getClass().getSimpleName().contains("AdvancementHolder"))
            try {
                handle = find.get("Advancement");
                find = ReflectionUtils.from(handle);
            } catch (Exception ignored) {}

        try {
            rewards = find.get("AdvancementRewards");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            criteria = find.get(Map.class);
        } catch (Exception ignored) {}

        try {
            Object newer = find.get("AdvancementRequirements");

            Object before = newer != null ?
                    ReflectionUtils.from(newer).get(List.class) :
                    find.get(String[][].class);

            if (before instanceof String[][]) {
                List<List<String>> list = new ArrayList<>();

                for (String[] s : (String[][]) before)
                    list.add(new ArrayList<>(Arrays.asList(s)));

                before = list;
            }

            requirements = (List<List<String>>) before;
        } catch (Exception ignored) {}

        try {
            Class<?> keyClass = ReflectionUtils.MC_VS >= 17.0 ?
                    ReflectionUtils.clazz("net.minecraft.resources.MinecraftKey") :
                    ReflectionUtils.getNmsClass("MinecraftKey");

            Object parentKey = find.get(find.get(handle.getClass()), keyClass);

            String namespace = (String) keyClass
                    .getMethod("getNamespace").invoke(parentKey);
            String key = (String) keyClass
                    .getMethod("getKey").invoke(parentKey);

            parent = Bukkit.getAdvancement(new NamespacedKey(namespace, key));
        } catch (Exception ignored) {}
    }
}
