package me.croabeast.advancement;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class providing reflection methods to facilitate interaction with internal
 * Bukkit and Minecraft server classes.
 * <p>
 * This class contains helper methods for retrieving classes from Bukkit, NMS (net.minecraft.server),
 * and for locating and accessing fields via reflection. It is designed to simplify the process of
 * working with server internals, which may vary between Minecraft versions.
 * </p>
 *
 * <p>
 * Key features include:
 * <ul>
 *   <li>Retrieving Bukkit classes based on the server package.</li>
 *   <li>Retrieving NMS classes using the server version extracted from Bukkit's version string.</li>
 *   <li>A nested {@link FieldFinder} class to search and retrieve fields from an object's class.</li>
 * </ul>
 * </p>
 *
 * @see Field
 * @see Bukkit#getVersion()
 */
@UtilityClass
class ReflectionUtils {

    /**
     * The package name of the CraftBukkit server implementation.
     */
    final String CRAFT_BUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();

    /**
     * The Minecraft server version extracted from the Bukkit version string.
     * <p>
     * The version is parsed using a regular expression to match a version number in the format "1.x"
     * and is returned as a {@code double}. If the version cannot be determined, it defaults to 0.0.
     * </p>
     */
    final double MC_VS = ((Supplier<Double>) () -> {
        Matcher m = Pattern.compile("1\\.(\\d+(\\.\\d+)?)").matcher(Bukkit.getVersion());
        if (!m.find()) return 0.0;
        try {
            return Double.parseDouble(m.group(1));
        } catch (Exception e) {
            return 0.0;
        }
    }).get();

    /**
     * Attempts to load a class by its fully qualified name.
     *
     * @param name the fully qualified class name.
     * @return the {@link Class} object if found; {@code null} otherwise.
     */
    @Nullable
    Class<?> clazz(String name) {
        try {
            return Class.forName(name);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Retrieves a class from the CraftBukkit package using the specified simple name.
     *
     * @param name the simple name of the class.
     * @return the {@link Class} object from the CraftBukkit package if found; {@code null} otherwise.
     */
    @Nullable
    Class<?> fromBukkit(String name) {
        return clazz(CRAFT_BUKKIT_PACKAGE + '.' + name);
    }

    /**
     * Retrieves an NMS (net.minecraft.server) class using the specified simple name.
     * <p>
     * The method constructs the fully qualified name by combining the NMS base package, the Minecraft
     * server version extracted from the CraftBukkit package, and the provided name.
     * </p>
     *
     * @param name the simple name of the NMS class.
     * @return the {@link Class} object if found; {@code null} otherwise.
     */
    @Nullable
    Class<?> getNmsClass(String name) {
        return clazz("net.minecraft.server" + "." + CRAFT_BUKKIT_PACKAGE.split("\\.")[3] + "." + name);
    }

    /**
     * Creates a new {@link FieldFinder} for the given parent object.
     *
     * @param parent the object whose fields will be searched.
     * @return a new {@link FieldFinder} instance.
     */
    static FieldFinder from(Object parent) {
        return new FieldFinder(parent);
    }

    /**
     * Utility class to locate and access fields of an object via reflection.
     * <p>
     * {@code FieldFinder} scans the declared fields of the parent's class and provides methods to search for
     * a field by type, by partial name, or by exact name. It also allows retrieving the value of a found field.
     * </p>
     */
    @SuppressWarnings("unchecked")
    static class FieldFinder {

        private final Field[] fields;
        private final Object parent;

        /**
         * Constructs a new {@code FieldFinder} for the specified parent object.
         *
         * @param parent the object whose fields will be searched.
         */
        private FieldFinder(Object parent) {
            this.parent = parent;
            fields = parent.getClass().getDeclaredFields();
        }

        /**
         * Returns the class of the parent object.
         *
         * @return the {@link Class} of the parent.
         */
        public Class<?> getType() {
            return parent.getClass();
        }

        /**
         * Searches for the first field in the parent's class whose type exactly matches the target type.
         *
         * @param target the target field type.
         * @return the {@link Field} if found; {@code null} otherwise.
         */
        @Nullable
        public Field search(Class<?> target) {
            for (Field field : fields) {
                if (field.getType() != target)
                    continue;
                field.setAccessible(true);
                return field;
            }
            return null;
        }

        /**
         * Searches for the first field in the parent's class whose simple name contains the specified string.
         *
         * @param clazz a string to match against the field's type simple name.
         * @return the {@link Field} if found; {@code null} otherwise.
         */
        @Nullable
        public Field search(String clazz) {
            for (Field field : fields) {
                if (!field.getType().getSimpleName().contains(clazz))
                    continue;
                field.setAccessible(true);
                return field;
            }
            return null;
        }

        /**
         * Searches for a field by its exact name in the parent's class.
         *
         * @param name the exact name of the field.
         * @return the {@link Field} if found; {@code null} otherwise.
         */
        @Nullable
        public Field searchForName(String name) {
            try {
                Field field = getType().getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * Retrieves the value of the field of the specified target type from the given parent object.
         *
         * @param parent the object from which to retrieve the field value.
         * @param target the expected type of the field.
         * @param <T>    the type parameter of the field.
         * @return the field value of type {@code T}.
         * @throws Exception if the field cannot be found or accessed.
         */
        public <T> T get(Object parent, Class<T> target) throws Exception {
            return (T) Objects.requireNonNull(search(target)).get(parent);
        }

        /**
         * Retrieves the value of the field of the specified target type from the parent's object.
         *
         * @param target the expected type of the field.
         * @param <T>    the type parameter of the field.
         * @return the field value of type {@code T}.
         * @throws Exception if the field cannot be found or accessed.
         */
        public <T> T get(Class<T> target) throws Exception {
            return get(parent, target);
        }

        /**
         * Retrieves the value of the field whose type's simple name contains the specified string from the given parent object.
         *
         * @param parent the object from which to retrieve the field value.
         * @param target a string to match against the field's type simple name.
         * @param <T>    the type parameter of the field.
         * @return the field value of type {@code T}.
         * @throws Exception if the field cannot be found or accessed.
         */
        public <T> T get(Object parent, String target) throws Exception {
            return (T) Objects.requireNonNull(search(target)).get(parent);
        }

        /**
         * Retrieves the value of the field whose type's simple name contains the specified string from the parent's object.
         *
         * @param target a string to match against the field's type simple name.
         * @param <T>    the type parameter of the field.
         * @return the field value of type {@code T}.
         * @throws Exception if the field cannot be found or accessed.
         */
        public <T> T get(String target) throws Exception {
            return get(parent, target);
        }

        /**
         * Retrieves the value of a field by its exact name from the given parent object.
         *
         * @param parent the object from which to retrieve the field value.
         * @param name   the exact name of the field.
         * @param <T>    the type parameter of the field.
         * @return the field value of type {@code T}.
         * @throws Exception if the field cannot be found or accessed.
         */
        public <T> T byName(Object parent, String name) throws Exception {
            return (T) Objects.requireNonNull(searchForName(name)).get(parent);
        }

        /**
         * Retrieves the value of a field by its exact name from the parent's object.
         *
         * @param name the exact name of the field.
         * @param <T>  the type parameter of the field.
         * @return the field value of type {@code T}.
         * @throws Exception if the field cannot be found or accessed.
         */
        public <T> T byName(String name) throws Exception {
            return byName(parent, name);
        }
    }
}
