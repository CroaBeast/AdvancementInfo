package me.croabeast.advancementinfo;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;

public abstract class NMSHandler {

    /**
     * Server version. Example: 1.16.5
     */
    private static final String VERSION = Bukkit.getBukkitVersion().split("-")[0];
    /**
     * Server major version. Example: if {@link #VERSION} is 1.16.5, the result will be 16.
     */
    public static final int MAJOR_VERSION = Integer.parseInt(VERSION.split("\\.")[1]);

    @Nullable
    protected Class<?> getNMSClass(String pack, String name, boolean hasVersion) {
        Class<?> bukkitClass = Bukkit.getServer().getClass();
        String version = bukkitClass.getPackage().getName().split("\\.")[3];

        pack = pack != null ? pack : "net.minecraft.server";

        try {
            return Class.forName(pack + (hasVersion ? "." + version : "") + "." + name);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    protected Class<?> getBukkitClass(String name) {
        return getNMSClass("org.bukkit.craftbukkit", name, true);
    }

    @Nullable
    protected Object getObject(@Nullable Class<?> clazz, Object initial, String method) {
        try {
            clazz = clazz != null ? clazz : initial.getClass();
            return clazz.getDeclaredMethod(method).invoke(initial);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    protected Object getObject(Object initial, String method) {
        return getObject(null, initial, method);
    }

    @Nullable
    protected Object getBukkitItem(Object nmsItem) {
        Class<?> clazz = getBukkitClass("inventory.CraftItemStack");
        if (clazz == null) return null;

        Constructor<?> ct;
        try {
            ct = clazz.getDeclaredConstructor(nmsItem.getClass());
        } catch (NoSuchMethodException e) {
            return null;
        }

        ct.setAccessible(true);
        try {
            return ct.newInstance(nmsItem);
        } catch (Exception e) {
            return null;
        }
    }

    protected String checkValue(Object value, String def) {
        return value == null ? def : value.toString();
    }

    @Nullable
    protected String checkValue(Object value) {
        return checkValue(value, null);
    }
}