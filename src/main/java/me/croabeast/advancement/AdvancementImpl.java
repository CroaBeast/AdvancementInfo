package me.croabeast.advancement;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;

/**
 * An abstract implementation of {@link AdvancementInfo} that provides common functionality
 * for retrieving advancement details from a Bukkit {@link Advancement} object via reflection.
 * <p>
 * This class extracts internal data (handle, criteria, rewards, requirements, and parent advancement)
 * from a Bukkit advancement, using reflection to access fields from CraftBukkit internals.
 * The extracted data is used to populate the corresponding properties in the {@code AdvancementInfo}
 * interface.
 * </p>
 * <p>
 * Note: This implementation relies on reflection and may be sensitive to changes in the server internals
 * between Minecraft versions.
 * </p>
 *
 * @see AdvancementInfo
 */
@Getter
abstract class AdvancementImpl implements AdvancementInfo {

    /**
     * The Bukkit {@link Advancement} object representing this advancement.
     */
    private final Advancement bukkit;

    /**
     * The internal handle of the advancement retrieved via reflection.
     */
    @Getter(AccessLevel.NONE)
    protected Object handle;

    /**
     * The parent advancement of this advancement, if available.
     */
    private Advancement parent = null;

    /**
     * A map containing the criteria required to achieve this advancement.
     */
    @NotNull
    private Map<String, Object> criteria = new HashMap<>();

    /**
     * The rewards granted upon completing this advancement.
     */
    @Nullable
    private Object rewards = null;

    /**
     * A list of lists representing the requirements for this advancement.
     */
    @Nullable
    private List<List<String>> requirements = null;

    /**
     * Constructs a new {@code AdvancementImpl} instance by extracting internal data
     * from the provided Bukkit {@link Advancement} object.
     * <p>
     * This constructor uses reflection to:
     * <ul>
     *   <li>Obtain the internal "handle" of the advancement via the CraftBukkit implementation.</li>
     *   <li>Retrieve the rewards and criteria associated with the advancement.</li>
     *   <li>Extract the requirements, converting them into a list of lists if necessary.</li>
     *   <li>Attempt to identify and set the parent advancement using its Minecraft key.</li>
     * </ul>
     * If any of these reflective operations fail, exceptions may be thrown or ignored, resulting
     * in default values.
     * </p>
     *
     * @param advancement the Bukkit {@link Advancement} (must not be {@code null}).
     * @throws IllegalStateException if critical reflection operations fail.
     */
    @SuppressWarnings("all")
    AdvancementImpl(Advancement advancement) {
        bukkit = Objects.requireNonNull(advancement);

        // Retrieve the CraftAdvancement class from Bukkit via ReflectionUtils.
        Class<?> craft;
        try {
            craft = ReflectionUtils.fromBukkit("advancement.CraftAdvancement");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        // Obtain the internal handle using the getHandle method.
        try {
            Method method = Objects.requireNonNull(craft).getMethod("getHandle");
            handle = method.invoke(craft.cast(advancement));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        // Use FieldFinder to extract criteria, rewards, and requirements from the handle.
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
            Object before = newer != null
                    ? ReflectionUtils.from(newer).get(List.class)
                    : find.get(String[][].class);

            if (before instanceof String[][]) {
                List<List<String>> list = new ArrayList<>();

                for (String[] s : (String[][]) before)
                    list.add(new ArrayList<>(Arrays.asList(s)));
                before = list;
            }

            requirements = (List<List<String>>) before;
        } catch (Exception ignored) {}

        // Attempt to retrieve the parent advancement using the MinecraftKey.
        try {
            Class<?> keyClass = ReflectionUtils.MC_VS >= 17.0
                    ? ReflectionUtils.clazz("net.minecraft.resources.MinecraftKey")
                    : ReflectionUtils.getNmsClass("MinecraftKey");

            Object parentKey = find.get(find.get(handle.getClass()), keyClass);
            String namespace = (String) keyClass.getMethod("getNamespace").invoke(parentKey);
            String key = (String) keyClass.getMethod("getKey").invoke(parentKey);

            parent = Bukkit.getAdvancement(new NamespacedKey(namespace, key));
        } catch (Exception ignored) {}
    }
}
