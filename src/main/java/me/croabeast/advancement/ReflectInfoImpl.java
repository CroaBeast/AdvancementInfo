package me.croabeast.advancement;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.advancement.Advancement;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * A concrete implementation of {@link AdvancementInfo} that retrieves advancement display data
 * via reflection from Minecraft’s internal representation.
 * <p>
 * {@code ReflectInfoImpl} is used when native Bukkit methods are insufficient for accessing the advancement's
 * display information. It extracts the title, description, icon, display position (x, y), visibility settings,
 * and frame type by reflecting on the internal fields of the advancement's display handle.
 * </p>
 * <p>
 * The implementation converts internal chat components into plain text using reflection,
 * and it also converts an internal NMS item representation into a Bukkit {@link ItemStack}.
 * </p>
 *
 * @see AdvancementInfo
 */
@Getter
class ReflectInfoImpl extends AdvancementImpl {

    /**
     * The title of the advancement, obtained by converting an internal chat component.
     */
    @NotNull
    private final String title;

    /**
     * The description of the advancement, obtained by converting an internal chat component.
     */
    @NotNull
    private final String description;

    /**
     * The icon representing the advancement, converted from an internal NMS item.
     */
    @Nullable
    private final ItemStack icon;

    /**
     * Indicates whether a toast notification should be displayed when this advancement is achieved.
     */
    @Getter(AccessLevel.NONE)
    private final boolean showToast;

    /**
     * Indicates whether the advancement's completion is announced in chat.
     */
    @Getter(AccessLevel.NONE)
    private final boolean announceChat;

    /**
     * The x-coordinate for displaying this advancement.
     */
    private final float x;

    /**
     * The y-coordinate for displaying this advancement.
     */
    private final float y;

    /**
     * Indicates whether the advancement is hidden.
     */
    private final boolean hidden;

    /**
     * The frame type of the advancement, indicating its visual style.
     */
    private final Frame frame;

    /**
     * Helper method to convert an internal chat component to plain text.
     * <p>
     * This method uses reflection to call a method (either "toPlainText" or "getString")
     * on the internal chat component, and returns the resulting string. If conversion fails,
     * a default string is returned.
     * </p>
     *
     * @param object the internal chat component object.
     * @param def    the default string to return if conversion fails.
     * @return the plain text representation of the chat component, or the default string.
     */
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

    /**
     * Converts an internal NMS item representation to a Bukkit {@link ItemStack}.
     *
     * @param nmsItem the internal item object.
     * @return the corresponding {@link ItemStack}, or {@code null} if conversion fails.
     */
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

    /**
     * Constructs a new {@code ReflectInfoImpl} by extracting advancement display information via reflection.
     * <p>
     * This constructor extracts display information from the advancement's internal handle using
     * {@link ReflectionUtils.FieldFinder} to retrieve fields for title, description, icon, display coordinates,
     * toast and chat announcement settings, hidden state, and frame type.
     * </p>
     *
     * @param advancement the Bukkit {@link Advancement} to process (must not be {@code null}).
     * @throws Exception if a critical reflective operation fails.
     */
    ReflectInfoImpl(Advancement advancement) throws Exception {
        super(advancement);

        // Retrieve display data from the internal advancement handle.
        ReflectionUtils.FieldFinder find = ReflectionUtils.from(handle);
        find = ReflectionUtils.from(find.get("AdvancementDisplay"));

        // Format the key from the advancement's key.
        String key = getBukkit().getKey().toString();
        key = key.substring(key.lastIndexOf('/') + 1).replace('_', ' ');
        key = Arrays.stream(key.split(" "))
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));

        // Extract title and description from internal fields.
        this.title = fromComponent(find.byName("a"), key);
        String d = fromComponent(find.byName("b"), "No description.");
        this.description = d.replaceAll("\\\\n", " ");

        // Retrieve the icon.
        this.icon = getItem(find.byName("c"));

        // Retrieve display coordinates.
        this.x = find.byName("i");
        this.y = find.byName("j");

        // Retrieve display options: toast, chat announcement, and hidden status.
        this.showToast = find.byName("f");
        this.announceChat = find.byName("g");
        this.hidden = find.byName("h");

        // Retrieve and convert the frame type.
        final Object type = find.byName("e");
        this.frame = Frame.fromName(type != null ? type.toString() : null);
    }

    /**
     * Indicates whether the advancement should display a toast notification.
     *
     * @return {@code true} if a toast should be shown; {@code false} otherwise.
     */
    public boolean doesShowToast() {
        return showToast;
    }

    /**
     * Indicates whether the advancement should announce its completion to chat.
     *
     * @return {@code true} if it should announce to chat; {@code false} otherwise.
     */
    public boolean doesAnnounceToChat() {
        return announceChat;
    }

    /**
     * Returns a string representation of this ReflectInfoImpl instance.
     * <p>
     * The representation includes the advancement key and, if available, its parent's key.
     * </p>
     *
     * @return a string representation of this advancement info.
     */
    @Override
    public String toString() {
        Advancement p = getParent();
        return "ReflectAdvancementInfo{bukkit=" + getBukkit().getKey() + ", parent=" + (p == null ? null : p.getKey()) + '}';
    }
}
