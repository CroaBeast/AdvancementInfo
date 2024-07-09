package me.croabeast.lib.advancement;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.advancement.Advancement;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

    private static String fromChatComponent(Object display, String field, String def) {
        Object object = ReflectionUtils.fromField(field, display);
        if (object == null) return def;

        Class<?> chat = ReflectionUtils.MC_VS >= 17.0 ?
                ReflectionUtils.from("net.minecraft.network.chat.IChatBaseComponent") :
                ReflectionUtils.getNmsClass("IChatBaseComponent");

        if (chat == null) return def;

        String methodName = ReflectionUtils.MC_VS < 13.0 ? "toPlainText" : "getString";
        try {
            return chat.getMethod(methodName).invoke(object).toString();
        } catch (Exception e) {
            return def;
        }
    }

    private static ItemStack getItem(Object display) {
        Object nmsItem = ReflectionUtils.fromField("c", display);
        if (nmsItem == null) return null;

        Class<?> clazz = ReflectionUtils.fromCraftBukkit("inventory.CraftItemStack");
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

    ReflectInfoImpl(Advancement advancement) {
        super(advancement);

        Class<?> clazz = ReflectionUtils.MC_VS >= 17.0 ?
                ReflectionUtils.from("net.minecraft.advancements.AdvancementDisplay") :
                ReflectionUtils.getNmsClass("AdvancementDisplay");

        Object display = null;

        Field[] handleFields = handle.getClass().getDeclaredFields();
        for (Field field : handleFields) {
            if (field.getType() != clazz) continue;

            display = ReflectionUtils.fromField(field, handle, Object.class, null);
            break;
        }

        String key = getBukkit().getKey().toString();

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

        this.showToast = ReflectionUtils.fromField("f", display, true);
        this.announceChat = ReflectionUtils.fromField("g", display, true);
        this.hidden = ReflectionUtils.fromField("g", display, false);

        this.x = ReflectionUtils.fromField("i", display, 0F);
        this.y = ReflectionUtils.fromField("j", display, 0F);

        Object type = ReflectionUtils.fromField("e", display);
        this.frame = Frame.from(type != null ? type.toString() : null);
    }

    public boolean doesShowToast() {
        return showToast;
    }

    public boolean doesAnnounceToChat() {
        return announceChat;
    }
}
