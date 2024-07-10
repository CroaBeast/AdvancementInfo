package me.croabeast.lib.advancement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.advancement.Advancement;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.function.Supplier;

@Getter
final class PaperInfoImpl extends AdvancementImpl {

    @NotNull
    private final String title, description;
    @Nullable
    private final ItemStack icon;

    @Getter(AccessLevel.NONE)
    private final boolean showToast, announceChat;

    private final Frame frame;
    private final boolean hidden;

    @SneakyThrows
    PaperInfoImpl(Advancement advancement) {
        super(((Supplier<Advancement>) () -> {
            try {
                Class.forName(ReflectionUtils.MC_VS >= 12.0 ?
                        "com.destroystokyo.paper.ParticleBuilder" :
                        "io.papermc.paperclip.Paperclip");
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }

            return advancement;
        }).get());

        Object display;
        try {
            Method m = advancement.getClass().getMethod("getDisplay");
            display = m.invoke(advancement);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        title = deserialize(display, true);
        description = deserialize(display, false);

        icon = getValue(display, "icon");

        showToast = getValue(display, "doesShowToast");
        hidden = getValue(display, "isHidden");
        announceChat = getValue(display, "doesAnnounceToChat");

        final Object type = getValue(display, "frame");
        frame = Frame.from(
                type != null ?
                        type.toString() :
                        null
        );
    }

    @SuppressWarnings("unchecked")
    <T> T getValue(Object display, String name) throws Exception {
        return (T) display.getClass().getMethod(name).invoke(display);
    }

    @SneakyThrows
    String deserialize(Object display, boolean isTitle) {
        return LegacyComponentSerializer
                .legacyAmpersand()
                .serialize(getValue(display, isTitle ? "title" : "description"));
    }

    public boolean doesShowToast() {
        return showToast;
    }

    public boolean doesAnnounceToChat() {
        return announceChat;
    }

    public float getX() {
        return 0;
    }

    public float getY() {
        return 0;
    }

    @Override
    public String toString() {
        Advancement p = getParent();
        return "PaperAdvancementInfo{bukkit=" + getBukkit().getKey() + ", parent=" + (p == null ? null : p.getKey()) + '}';
    }
}
