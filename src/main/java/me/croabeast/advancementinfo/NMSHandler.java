package me.croabeast.advancementinfo;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;

/**
 * The class that handles some NMS/Reflection methods.
 * @author CroaBeast
 * @since 1.0
 */
abstract class NMSHandler {

    static double getVersion() {
        String[] vArray = Bukkit.getVersion().split("[.]");
        if (vArray.length < 3) return 0.0;

        try {
            int minor = Integer.parseInt(vArray[1]);
            int patch = Integer.parseInt(vArray[2]);
            return Double.parseDouble(minor + "." + patch);
        }
        catch (Exception e) {
            return 0.0;
        }
    }

    @Nullable
    static Class<?> getNMSClass(String pack, String name, boolean useVs) {
        Package aPackage = Bukkit.getServer().getClass().getPackage();

        String version = aPackage.getName().split("\\.")[3];
        pack = pack != null ? pack : "net.minecraft.server";

        try {
            return Class.forName(pack + (useVs ? "." + version : "") + "." + name);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    static Class<?> getBukkitClass(String name) {
        return getNMSClass("org.bukkit.craftbukkit", name, true);
    }

    @Nullable
    static Object getObject(@Nullable Class<?> clazz, Object initial, String method) {
        try {
            clazz = clazz != null ? clazz : initial.getClass();
            return clazz.getDeclaredMethod(method).invoke(initial);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    static Object getObject(Object initial, String method, String... extraArgs) {
        Object obj = getObject(null, initial, method);

        if (extraArgs == null || extraArgs.length == 0)
            return obj;

        for (String arg : extraArgs)
            obj = getObject(obj, arg);

        return obj;
    }

    @Nullable
    static ItemStack getBukkitItem(Object nmsItem) {
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
            return (ItemStack) ct.newInstance(nmsItem);
        } catch (Exception e) {
            return null;
        }
    }

    static String checkValue(Object value, String def) {
        return value == null ? def : value.toString();
    }

    @Nullable
    static String checkValue(Object value) {
        return checkValue(value, null);
    }
}
