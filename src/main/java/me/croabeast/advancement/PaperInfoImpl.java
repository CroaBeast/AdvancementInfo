package me.croabeast.advancement;

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

/**
 * A concrete implementation of {@link AdvancementInfo} for Paper servers.
 * <p>
 * {@code PaperInfoImpl} extracts advancement display data using Paper-specific methods and the
 * {@link LegacyComponentSerializer} for converting legacy formatted text into plain text.
 * It retrieves the title, description, icon, toast and chat announcement settings, frame type,
 * and hidden state from the {@code AdvancementDisplay} object of a Bukkit {@link Advancement}.
 * </p>
 * <p>
 * This implementation uses reflection to invoke methods on the display object, and it wraps the advancement
 * inside a {@link Supplier} to ensure compatibility with Paper's modifications.
 * </p>
 *
 * @see AdvancementInfo
 */
@Getter
final class PaperInfoImpl extends AdvancementImpl {

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
     * Indicates whether a toast notification should be shown when the advancement is achieved.
     */
    @Getter(AccessLevel.NONE)
    private final boolean showToast;

    /**
     * Indicates whether the advancement's completion is announced in chat.
     */
    @Getter(AccessLevel.NONE)
    private final boolean announceChat;

    /**
     * The frame type of the advancement.
     */
    private final Frame frame;

    /**
     * Indicates whether the advancement is hidden.
     */
    private final boolean hidden;

    /**
     * Constructs a new {@code PaperInfoImpl} by extracting display data from the provided advancement.
     * <p>
     * This constructor retrieves the {@code AdvancementDisplay} using reflection,
     * deserializes the title and description using {@link LegacyComponentSerializer},
     * and retrieves other display values (icon, toast, hidden, and announce settings).
     * It also determines the frame type from the display.
     * </p>
     *
     * @param advancement the Bukkit {@link Advancement} (must not be {@code null}).
     * @throws IllegalStateException if any critical reflection operation fails.
     */
    @SneakyThrows
    PaperInfoImpl(Advancement advancement) {
        // Wrap the advancement retrieval inside a Supplier to ensure compatibility with Paper.
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

        // Retrieve the display object using reflection.
        Object display;
        try {
            Method m = advancement.getClass().getMethod("getDisplay");
            display = m.invoke(advancement);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        // Deserialize the title and description using LegacyComponentSerializer.
        title = deserialize(display, true);
        description = deserialize(display, false);

        // Retrieve the icon from the display.
        icon = getValue(display, "icon");

        // Retrieve display options: showToast, hidden, and announceChat.
        showToast = getValue(display, "doesShowToast");
        hidden = getValue(display, "isHidden");
        announceChat = getValue(display, "doesAnnounceToChat");

        // Retrieve and convert the frame type.
        final Object type = getValue(display, "frame");
        frame = Frame.from(type != null ? type.toString() : null);
    }

    /**
     * Retrieves a value from the display object by invoking a method with the specified name.
     *
     * @param <T>     the expected type of the value.
     * @param display the display object.
     * @param name    the name of the method to invoke.
     * @return the value returned by the method.
     * @throws Exception if the method cannot be invoked.
     */
    @SuppressWarnings("unchecked")
    private <T> T getValue(Object display, String name) throws Exception {
        return (T) display.getClass().getMethod(name).invoke(display);
    }

    /**
     * Deserializes a chat component from the display object into a plain text string.
     * <p>
     * If {@code isTitle} is {@code true}, the method deserializes the title; otherwise, it deserializes the description.
     * </p>
     *
     * @param display the display object containing the chat components.
     * @param isTitle {@code true} to deserialize the title, {@code false} for the description.
     * @return the plain text string representing the chat component.
     * @throws Exception if the deserialization method cannot be invoked.
     */
    @SneakyThrows
    String deserialize(Object display, boolean isTitle) {
        return LegacyComponentSerializer.legacyAmpersand()
                .serialize(getValue(display, isTitle ? "title" : "description"));
    }

    /**
     * Indicates whether this advancement should show a toast notification.
     *
     * @return {@code true} if a toast should be shown; {@code false} otherwise.
     */
    public boolean doesShowToast() {
        return showToast;
    }

    /**
     * Indicates whether this advancement should announce its completion to chat.
     *
     * @return {@code true} if it should announce to chat; {@code false} otherwise.
     */
    public boolean doesAnnounceToChat() {
        return announceChat;
    }

    /**
     * Returns a string representation of this {@code PaperInfoImpl} instance.
     * <p>
     * The representation includes the advancement's key and, if available, its parent's key.
     * </p>
     *
     * @return a string representation of this advancement info.
     */
    @Override
    public String toString() {
        Advancement p = getParent();
        return "PaperAdvancementInfo{bukkit=" + getBukkit().getKey() + ", parent=" + (p == null ? null : p.getKey()) + '}';
    }

    /**
     * Note: The {@link #getX()} and {@link #getY()} methods are not overridden in this implementation and default to 0.
     *
     * @return 0.0 as the default x-coordinate.
     */
    public float getX() {
        return 0;
    }

    /**
     * Note: The {@link #getX()} and {@link #getY()} methods are not overridden in this implementation and default to 0.
     *
     * @return 0.0 as the default y-coordinate.
     */
    public float getY() {
        return 0;
    }
}
