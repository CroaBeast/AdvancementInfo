package me.croabeast.lib.advancement;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
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
    Class<?> from(String name) {
        try {
            return Class.forName(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    Class<?> fromCraftBukkit(String name) {
        return from(CRAFT_BUKKIT_PACKAGE + '.' + name);
    }

    @Nullable
    Class<?> getNmsClass(String name) {
        return from("net.minecraft.server" + "." + CRAFT_BUKKIT_PACKAGE.split("\\.")[3] + "." + name);
    }

    @Nullable
    Field getField(Object o, String name) {
        try {
            return o.getClass().getDeclaredField(name);
        } catch (Exception e) {
            return null;
        }
    }

    <T> T fromField(Field field, Object initial, Class<T> clazz, T def) {
        if (field == null) return def;

        try {
            if (!field.isAccessible()) field.setAccessible(true);
            return clazz.cast(field.get(initial));
        } catch (Exception e) {
            return def;
        }
    }

    @SuppressWarnings("unchecked")
    <T> T fromField(String field, Object initial, T def) {
        return fromField(getField(initial, field), initial, (Class<T>) def.getClass(), def);
    }

    @Nullable
    Object fromField(String field, Object initial) {
        return fromField(getField(initial, field), initial, Object.class, null);
    }
}
