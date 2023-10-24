package me.croabeast.advancementinfo;

import lombok.Getter;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class AdvancementInfo {

    private static final double MC_VERSION = ((Function<String, Double>) s -> {
        Matcher m = Pattern
                .compile("1\\.(\\d+(\\.\\d+)?)")
                .matcher(s);

        if (!m.find()) return 0.0;

        try {
            return Double.parseDouble(m.group(1));
        } catch (Exception e) {
            return 0.0;
        }
    }).apply(Bukkit.getVersion());

    private static final boolean IS_20_2 = MC_VERSION >= 20.2, IS_19_4 = MC_VERSION >= 19.4;

    private final Advancement bukkit;

    private final String title;
    private final String description;

    private final FrameType frame;

    private final boolean announcedToChat;
    private final boolean hidden;

    private final String parent;

    private final ItemStack item;

    private final Object rewards;
    private final Map<String, Object> criteria;
    private final String[][] requirements;

    @Nullable
    private static Class<?> getNMSClass(String pack, String name, boolean useVs) {
        Package aPackage = Bukkit.getServer().getClass().getPackage();

        String version = aPackage.getName().split("\\.")[3];
        pack = pack != null ? pack : "net.minecraft.server";

        try {
            return Class.forName(pack + (useVs ? "." + version : "") + "." + name);
        } catch (Exception e) {
            return null;
        }
    }

    private static Class<?> getBukkitClass(String name) {
        return getNMSClass("org.bukkit.craftbukkit", name, true);
    }

    @Nullable
    private static Object getObject(@Nullable Class<?> clazz, Object initial, String method) {
        if (initial == null) return null;

        try {
            clazz = clazz != null ? clazz : initial.getClass();
            return clazz.getDeclaredMethod(method).invoke(initial);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static Object getObject(Object initial, String method, String... args) {
        Object obj = getObject(null, initial, method);

        if (args == null || args.length == 0) return obj;
        for (String arg : args)
            obj = getObject(obj, arg);

        return obj;
    }

    private static String getTitleOrDescription(Object object) {
        if (object == null) return null;

        Class<?> chat = getNMSClass(
                MC_VERSION >= 17.0 ? "net.minecraft.network.chat" : null,
                "IChatBaseComponent", MC_VERSION < 17.0
        );

        if (chat == null) return null;

        String method = MC_VERSION < 13.0 ? "toPlainText" : "getString";
        return String.valueOf(getObject(chat, object, method));
    }

    private static boolean bool(String string) {
        return string.matches("(?i)true|false") && string.matches("(?i)true");
    }

    private static final Function<Object, ItemStack> ITEM_FUNCTION = o -> {
        Field itemField = null;
        try {
            itemField = o.getClass().getDeclaredField("c");
        } catch (Exception ignored) {}

        Object nmsItem = null;
        if (itemField != null) {
            try {
                itemField.setAccessible(true);
                nmsItem = itemField.get(o);
                itemField.setAccessible(false);
            }
            catch (Exception ignored) {}
        }

        if (nmsItem == null) return null;

        Class<?> clazz = getBukkitClass("inventory.CraftItemStack");
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
    };

    @SuppressWarnings("unchecked")
    public AdvancementInfo(Advancement adv) {
        Class<?> craft = getBukkitClass("advancement.CraftAdvancement");
        if (craft == null)
            throw new IllegalStateException();

        this.bukkit = Objects.requireNonNull(adv);

        Object nmsAd = getObject(craft, craft.cast(adv), "getHandle");
        if (IS_20_2) nmsAd = getObject(nmsAd, "b");

        Object display = getObject(nmsAd, IS_19_4 ? "d" : "c");
        if (display == null)
            throw new IllegalStateException();

        if (IS_20_2) {
            Optional<?> o = ((Optional<?>) display);
            if (o.isPresent()) display = o.get();
        }

         String title = getTitleOrDescription(getObject(display, "a"));
        if (title == null) {
            String key = adv.getKey().toString();

            key = key.substring(key.lastIndexOf('/') + 1);
            key = key.replace('_', ' ');

            title = WordUtils.capitalizeFully(key);
        }

        this.title = title;

        String desc = getTitleOrDescription(getObject(display, "b"));
        description = desc == null ?
                "No description" : desc.replaceAll('\\' + "n", " ");

        Object f = getObject(display, "e");
        frame = FrameType.getFrameType(String.valueOf(f));

        announcedToChat = bool(String.valueOf(getObject(display, "i")));
        hidden = bool(String.valueOf(getObject(display, "j")));

        parent = String.valueOf(getObject(nmsAd, "b", "getName"));
        item = ITEM_FUNCTION.apply(display);

        rewards = getObject(nmsAd, IS_19_4 ? "e" : "d");

        String criteriaName = MC_VERSION < 18.0 ? "getCriteria" : "f";
        Object criteria = getObject(nmsAd, IS_19_4 ? "g" : criteriaName);

        this.criteria = criteria == null ?
                new HashMap<>() : (Map<String, Object>) criteria;

        Object req = getObject(nmsAd, IS_19_4 ? "j" : "i");
        this.requirements = req == null ? null : (String[][]) req;
    }

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
        return "AdvancementInfo{title='" + title + "', frameType='" + frame + "'}";
    }
}
