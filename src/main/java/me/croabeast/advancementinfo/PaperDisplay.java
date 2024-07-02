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

    @SneakyThrows
    PaperDisplay(Object display) {
        description = deserialize(display, false);
        title = deserialize(display, true);

        icon = getValue(display, "icon");

        showToast = getValue(display, "doesShowToast");
        announceChat = getValue(display, "doesAnnounceToChat");
        hidden = getValue(display, "isHidden");

        Object type = getValue(display, "frame");
        this.type = FrameType.getFrameType(type != null ? type.toString() : null);
    }

    @SuppressWarnings("unchecked")
    <T> T getValue(Object display, String name) throws Exception {
        return (T) display.getClass().getMethod(name).invoke(display);
    }

    static Class<?> fromText(String name) throws ClassNotFoundException {
        return Class.forName("net.kyori.adventure.text." + name);
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
