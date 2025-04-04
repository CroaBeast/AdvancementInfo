package me.croabeast.advancement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementDisplay;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * A concrete implementation of {@link AdvancementInfo} for Bukkit advancements.
 * <p>
 * {@code BukkitInfoImpl} extracts display information from a Bukkit {@link Advancement} using its
 * {@link AdvancementDisplay}. It retrieves the title, description, icon, display options (toast and chat announcement),
 * position (x, y), visibility, and frame type of the advancement.
 * </p>
 * <p>
 * This implementation relies on reflection to access the display data, and it is intended to be used when the server
 * version is compatible with the Bukkit API's advancement system.
 * </p>
 *
 * @see AdvancementInfo
 */
@Getter
final class BukkitInfoImpl extends AdvancementImpl {

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
     * The icon representing the advancement.
     */
    @Nullable
    private final ItemStack icon;

    /**
     * Indicates whether the advancement should show a toast notification.
     */
    @Getter(AccessLevel.NONE)
    private final boolean showToast;

    /**
     * Indicates whether the advancement completion is announced in chat.
     */
    @Getter(AccessLevel.NONE)
    private final boolean announceChat;

    /**
     * The x-coordinate for displaying the advancement.
     */
    private final float x;

    /**
     * The y-coordinate for displaying the advancement.
     */
    private final float y;

    /**
     * Indicates whether the advancement is hidden.
     */
    private final boolean hidden;

    /**
     * The frame type of the advancement.
     */
    private final Frame frame;

    /**
     * Constructs a new {@code BukkitInfoImpl} from the given Bukkit {@link Advancement}.
     * <p>
     * This constructor uses reflection to obtain the {@link AdvancementDisplay} and then extracts
     * the relevant display information such as title, description, icon, toast and chat announcement options,
     * display coordinates, hidden status, and frame type.
     * </p>
     *
     * @param advancement the Bukkit advancement (must not be {@code null}).
     * @throws IllegalStateException if the display information cannot be retrieved.
     */
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

    /**
     * Indicates whether this advancement should show a toast notification.
     *
     * @return {@code true} if a toast should be displayed; {@code false} otherwise.
     */
    public boolean doesShowToast() {
        return showToast;
    }

    /**
     * Indicates whether this advancement's completion should be announced in chat.
     *
     * @return {@code true} if it should announce to chat; {@code false} otherwise.
     */
    public boolean doesAnnounceToChat() {
        return announceChat;
    }

    /**
     * Returns a string representation of this advancement info.
     * <p>
     * The representation includes the key of the Bukkit advancement and, if available, the key of its parent advancement.
     * </p>
     *
     * @return a string representation of this {@code BukkitInfoImpl}.
     */
    @Override
    public String toString() {
        Advancement p = getParent();
        return "BukkitAdvancementInfo{bukkit=" + getBukkit().getKey() + ", parent=" + (p == null ? null : p.getKey()) + '}';
    }
}
