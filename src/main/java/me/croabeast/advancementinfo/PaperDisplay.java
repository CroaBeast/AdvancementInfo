package me.croabeast.advancementinfo;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
class PaperDisplay {

    @NotNull
    private final String title, description;
    @Nullable
    private final ItemStack icon;

    private final boolean showToast, announceChat, hidden;
    private final float x = 0, y = 0;

    private final FrameType type;

    static Class<?> fromText(String name) throws ClassNotFoundException {
        return Class.forName("net.kyori.adventure.text." + name);
    }

    @SneakyThrows
    PaperDisplay(Object display) {
        Class<?> paperDisplay = display.getClass();

        title = deserialize(display, true);
        description = deserialize(display, false);

        icon = getValue(display, "icon");

        showToast = getValue(display, "doesShowToast");
        announceChat = getValue(display, "doesAnnounceToChat");
        hidden = getValue(display, "isHidden");

        Object type = getValue(display, "frame");
        this.type = FrameType.getFrameType(type.toString());
    }


    @SuppressWarnings("unchecked")
    <T> T getValue(Object display, String name) throws Exception {
        return (T) display.getClass().getMethod(name).invoke(display);
    }

    String deserialize(Object display, boolean isTitle) {
        try {
            Class<?> clazz = fromText("serializer.legacy.LegacyComponentSerializer");
            Object ampersand = clazz.getMethod("legacyAmpersand").invoke(null);

            return (String) ampersand.getClass()
                    .getMethod("serialize", fromText("Component"))
                    .invoke(ampersand,
                            display.getClass()
                                    .getMethod(isTitle ? "title" : "description")
                                    .invoke(display)
                    );
        } catch (Exception e) {
            return null;
        }
    }
}
