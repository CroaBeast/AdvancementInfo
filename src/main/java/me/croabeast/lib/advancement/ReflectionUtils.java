package me.croabeast.lib.advancement;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
class ReflectionUtils {

    final String CRAFT_BUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();

    final double MC_VS = ((Supplier<Double>) () -> {
        Matcher m = Pattern
                .compile("1\\.(\\d+(\\.\\d+)?)")
                .matcher(Bukkit.getVersion());
        if (!m.find()) return 0.0;

        try {
            return Double.parseDouble(m.group(1));
        } catch (Exception e) {
            return 0.0;
        }
    }).get();

    @Nullable
    Class<?> clazz(String name) {
        try {
            return Class.forName(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    Class<?> fromBukkit(String name) {
        return clazz(CRAFT_BUKKIT_PACKAGE + '.' + name);
    }

    @Nullable
    Class<?> getNmsClass(String name) {
        return clazz("net.minecraft.server" + "." + CRAFT_BUKKIT_PACKAGE.split("\\.")[3] + "." + name);
    }

    static FieldFinder from(Object parent) {
        return new FieldFinder(parent);
    }

    @SuppressWarnings("unchecked")
    static class FieldFinder {

        private final Field[] fields;
        private final Object parent;

        private FieldFinder(Object parent) {
            this.parent = parent;
            fields = parent.getClass().getDeclaredFields();
        }

        public Class<?> getType() {
            return parent.getClass();
        }

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

        public <T> T get(Object parent, Class<T> target) throws Exception {
            return (T) Objects.requireNonNull(search(target)).get(parent);
        }

        public <T> T get(Class<T> target) throws Exception {
            return get(parent, target);
        }

        public <T> T get(Object parent, String target) throws Exception {
            return (T) Objects.requireNonNull(search(target)).get(parent);
        }

        public <T> T get(String target) throws Exception {
            return get(parent, target);
        }

        public <T> T byName(Object parent, String name) throws Exception {
            return (T) Objects.requireNonNull(searchForName(name)).get(parent);
        }

        public <T> T byName(String name) throws Exception {
            return byName(parent, name);
        }
    }
}
