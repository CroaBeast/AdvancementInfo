package me.croabeast.lib.advancement;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.advancement.Advancement;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.stream.Collectors;

@Getter
class ReflectInfoImpl extends AdvancementImpl {

    @NotNull
    private final String title, description;
    @Nullable
    private final ItemStack icon;

    @Getter(AccessLevel.NONE)
    private final boolean showToast, announceChat;

    private final float x, y;
    private final boolean hidden;

    private final Frame frame;

    private static String fromComponent(Object object, String def) {
        if (object == null) return def;

        Class<?> chat = ReflectionUtils.MC_VS >= 17.0 ?
                ReflectionUtils.clazz("net.minecraft.network.chat.IChatBaseComponent") :
                ReflectionUtils.getNmsClass("IChatBaseComponent");

        if (chat == null) return def;

        String methodName = ReflectionUtils.MC_VS < 13.0 ? "toPlainText" : "getString";
        try {
            return chat.getMethod(methodName).invoke(object).toString();
        } catch (Exception e) {
            return def;
        }
    }

    private static ItemStack getItem(Object nmsItem) {
        if (nmsItem == null) return null;

        Class<?> clazz = ReflectionUtils.fromBukkit("inventory.CraftItemStack");
        if (clazz == null) return null;

        Constructor<?> ct;
        try {
            ct = clazz.getDeclaredConstructor(nmsItem.getClass());
            ct.setAccessible(true);
        } catch (NoSuchMethodException e) {
            return null;
        }

        try {
            return (ItemStack) ct.newInstance(nmsItem);
        } catch (Exception e) {
            return null;
        }
    }

    ReflectInfoImpl(Advancement advancement) throws Exception {
        super(advancement);

        ReflectionUtils.FieldFinder find = ReflectionUtils.from(handle);
        find = ReflectionUtils.from(find.get("AdvancementDisplay"));

        String key = getBukkit().getKey().toString();

        key = key.substring(key.lastIndexOf('/') + 1);
        key = key.replace('_', ' ');

        key = Arrays.stream(key.split(" "))
                .map(s -> {
                    String first = s.substring(0, 1).toUpperCase();
                    return first + s.substring(1).toLowerCase();
                })
                .collect(Collectors.joining(" "));

        this.title = fromComponent(find.byName("a"), key);

        String d = fromComponent(find.byName("b"), "No description.");
        this.description = d.replaceAll('\\' + "n", " ");

        this.icon = getItem(find.byName("c"));

        this.x = find.byName("i");
        this.y = find.byName("j");

        this.showToast = find.byName("f");
        this.announceChat = find.byName("g");
        this.hidden = find.byName("h");

        final Object type = find.byName("e");
        this.frame = Frame.from(type != null ? type.toString() : null);
    }

    public boolean doesShowToast() {
        return showToast;
    }

    public boolean doesAnnounceToChat() {
        return announceChat;
    }

    @Override
    public String toString() {
        Advancement p = getParent();
        return "ReflectAdvancementInfo{bukkit=" + getBukkit().getKey() + ", parent=" + (p == null ? null : p.getKey()) + '}';
    }
}
