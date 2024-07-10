package me.croabeast.lib.advancement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementDisplay;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

@Getter
final class BukkitInfoImpl extends AdvancementImpl {

    @NotNull
    private final String title, description;
    @Nullable
    private final ItemStack icon;

    @Getter(AccessLevel.NONE)
    private final boolean showToast, announceChat;

    private final float x, y;
    private final boolean hidden;

    private final Frame frame;

    @SneakyThrows
    BukkitInfoImpl(Advancement advancement) {
        super(advancement);

        AdvancementDisplay display;
        try {
            Method m = advancement.getClass().getMethod("getDisplay");
            display = (AdvancementDisplay) m.invoke(advancement);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        this.title = display.getTitle();
        this.description = display.getDescription();

        this.icon = display.getIcon();

        this.showToast = display.shouldShowToast();
        this.announceChat = display.shouldAnnounceChat();
        this.hidden = display.isHidden();

        this.x = display.getX();
        this.y = display.getY();

        this.frame = Frame.from(display.getType().name());
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
        return "BukkitAdvancementInfo{bukkit=" + getBukkit().getKey() + ", parent=" + (p == null ? null : p.getKey()) + '}';
    }
}
