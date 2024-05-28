package me.croabeast.advancementinfo;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementDisplay;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class encapsulates detailed information about a Bukkit advancement in Minecraft.
 * It provides various properties such as the title, description, icon, criteria, rewards, etc.,
 * making it easier to interact with and manipulate advancement data programmatically.
 *
 * <p> The AdvancementInfo class acts as a bridge between the Bukkit API and the underlying
 * Minecraft advancement system, offering developers a more convenient and intuitive way
 * to access and manage advancement-related information.
 */
@SuppressWarnings("unchecked")
@Getter
public class AdvancementInfo {

    private static final double MC_VS = ((Supplier<Double>) () -> {
        Matcher m = Pattern
                .compile("1\\.(\\d+(\\.\\d+)?)")
                .matcher(Bukkit.getVersion());
        if (!m.find()) return 0.0;

        try {
            return Double.parseDouble(m.group(1));
        } catch (Exception e) {
            return 0.0;
        }
    }).get();

    private static Class<?> from(String name) {
        try {
            return Class.forName(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static Class<?> getNmsClass(String pack, String name) {
        return from(
                (pack != null ? pack : "net.minecraft.server") +
                        "." +
                        Bukkit.getServer().getClass()
                                .getPackage()
                                .getName().split("\\.")[3] +
                        "." + name
        );
    }

    @Nullable
    private static Class<?> getNmsClass(String name) {
        return getNmsClass(null, name);
    }

    private static Object getHandle(Advancement advancement) {
        String name = "advancement.CraftAdvancement";

        Class<?> craft = getNmsClass("org.bukkit.craftbukkit", name);
        if (craft == null) throw new NullPointerException();

        try {
            Method method = craft.getMethod("getHandle");
            return method.invoke(craft.cast(advancement));
        } catch (Exception e) {
            throw new NullPointerException(e.getLocalizedMessage());
        }
    }

    private static Field getField(Object o, String name) {
        try {
            return o.getClass().getDeclaredField(name);
        } catch (Exception e) {
            return null;
        }
    }

    private static <T> T fromField(Field field, Object initial, Class<T> clazz, T def) {
        if (field == null) return def;

        try {
            if (!field.isAccessible()) field.setAccessible(true);
            return clazz.cast(field.get(initial));
        } catch (Exception e) {
            return def;
        }
    }

    private static <T> T fromField(String field, Object initial, T def) {
        return fromField(getField(initial, field), initial, (Class<T>) def.getClass(), def);
    }

    private static Object fromField(String field, Object initial) {
        return fromField(getField(initial, field), initial, Object.class, null);
    }

    private static String fromChatComponent(Object display, String field, String def) {
        Object object = fromField(field, display);
        if (object == null) return def;

        Class<?> chat = MC_VS >= 17.0 ?
                from("net.minecraft.network.chat.IChatBaseComponent") :
                getNmsClass("IChatBaseComponent");

        if (chat == null) return def;

        String methodName = MC_VS < 13.0 ? "toPlainText" : "getString";

        try {
            return chat.getMethod(methodName).invoke(object).toString();
        } catch (Exception e) {
            return def;
        }
    }

    private static ItemStack getItem(Object display) {
        Object nmsItem = fromField("c", display);
        if (nmsItem == null) return null;

        Class<?> clazz = getNmsClass("org.bukkit.craftbukkit", "inventory.CraftItemStack");
        if (clazz == null) return null;

        Constructor<?> ct;
        try {
            ct = clazz.getDeclaredConstructor(nmsItem.getClass());
        } catch (NoSuchMethodException e) {
            return null;
        }

        ct.setAccessible(true);
        try {
            return (ItemStack) ct.newInstance(nmsItem);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * The Bukkit advancement object.
     */
    @NotNull
    private final Advancement bukkit;

    /**
     * The title of the advancement.
     */
    @NotNull
    private final String title;

    /**
     * The description of the advancement.
     */
    @NotNull
    private final String description;

    /**
     * The icon associated with the advancement.
     */
    @Nullable
    private final ItemStack icon;

    /**
     * Indicates whether to show a toast when the advancement is achieved.
     */
    private final boolean showToast;

    /**
     * Indicates whether to announce in chat when the advancement is achieved.
     */
    private final boolean announceChat;

    /**
     * Indicates whether the advancement is hidden.
     */
    private final boolean hidden;

    /**
     * The X-coordinate of the advancement on the advancement screen.
     */
    private final float x;

    /**
     * The Y-coordinate of the advancement on the advancement screen.
     */
    private final float y;

    /**
     * The type of advancement frame.
     */
    @NotNull
    private final FrameType type;

    /**
     * Criteria required to achieve the advancement.
     */
    @NotNull
    private final Map<String, Object> criteria;

    /**
     * Rewards granted upon achieving the advancement.
     */
    @Nullable
    private final Object rewards;

    /**
     * Requirements for the advancement.
     */
    @Nullable
    private final String[][] requirements;

    /**
     * Constructs an AdvancementInfo object from a Bukkit advancement.
     *
     * @param advancement The Bukkit advancement object.
     */
    public AdvancementInfo(Advancement advancement) {
        this.bukkit = Objects.requireNonNull(advancement);

        Object handle = getHandle(advancement);

        Field criteriaField = null;
        Field rField = null;
        Field rewardsField = null;

        Field[] handleFields = handle.getClass().getDeclaredFields();

        Class<?> rewardsClass = MC_VS >= 17.0 ?
                from("net.minecraft.advancements.AdvancementRewards") :
                getNmsClass("AdvancementRewards");

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
                fromField(criteriaField, handle, Map.class, new HashMap<>());

        this.rewards = fromField(rewardsField, handle, Object.class, null);
        this.requirements = fromField(rField, handle, String[][].class, null);

        Object previous = null;
        try {
            Method m = advancement.getClass().getMethod("getDisplay");
            previous = m.invoke(advancement);
        } catch (Exception ignored) {}

        if (MC_VS >= 18.0 && previous != null) {
            AdvancementDisplay parent = (AdvancementDisplay) previous;

            this.title = parent.getTitle();
            this.description = parent.getDescription();

            this.icon = parent.getIcon();

            this.showToast = parent.shouldShowToast();
            this.announceChat = parent.shouldAnnounceChat();
            this.hidden = parent.isHidden();

            this.x = parent.getX();
            this.y = parent.getY();

            this.type = FrameType.getFrameType(parent.getType().name());
            return;
        }

        Class<?> clazz = MC_VS >= 17.0 ?
                from("net.minecraft.advancements.AdvancementDisplay") :
                getNmsClass("AdvancementDisplay");

        Object display = null;

        for (Field field : handleFields) {
            if (field.getType() != clazz) continue;

            display = fromField(field, handle, Object.class, null);
            break;
        }

        String key = bukkit.getKey().toString();

        key = key.substring(key.lastIndexOf('/') + 1);
        key = key.replace('_', ' ');

        key = Arrays.stream(key.split(" "))
                .map(s -> {
                    String first = s.substring(0, 1).toUpperCase();
                    return first + s.substring(1).toLowerCase();
                })
                .collect(Collectors.joining(" "));

        this.title = fromChatComponent(display, "a", key);

        String d = fromChatComponent(display, "b", "No description.");
        this.description = d.replaceAll('\\' + "n", " ");

        this.icon = getItem(display);

        this.showToast = fromField("f", display, true);
        this.announceChat = fromField("g", display, true);
        this.hidden = fromField("g", display, false);

        this.x = fromField("i", display, 0F);
        this.y = fromField("j", display, 0F);

        this.type = FrameType.getFrameType(fromField("e", display).toString());
    }

    /**
     * Gets the description of the advancement as an array of strings with a
     * maximum length per line.
     *
     * @param length The maximum length of each line.
     * @return The description array.
     */
    @NotNull
    public String[] getDescriptionArray(int length) {
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

    @Override
    public String toString() {
        return "AdvancementInfo{title='" + title + "', frameType='" + type + "'}";
    }
}
