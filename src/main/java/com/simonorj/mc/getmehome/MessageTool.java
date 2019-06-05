package com.simonorj.mc.getmehome;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class MessageTool {
    private static ClassLoader loader = null;
    private static String base(I18n i18n, Locale locale, Object... args) {
        String msg = getBundleString(i18n, locale);

        return String.format(msg, args);
    }

    private static String getBundleString(I18n i18n, Locale locale) {
        if (loader != null) {
            try {
                return ResourceBundle.getBundle("GetMeHome", locale, loader).getString(i18n.toString());
            } catch (NullPointerException | MissingResourceException ignore) {}
        }

        return ResourceBundle.getBundle("i18n.GetMeHome", locale).getString(i18n.toString());
    }

    static void reloadI18n(File i18nFolder) {
        try {
            URL[] urls = {i18nFolder.toURI().toURL()};
            loader = new URLClassLoader(urls);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            loader = null;
        }
    }

    public static String raw(I18n i18n, CommandSender p, Object... args) {
        return base(i18n, getLocale(p), args);
    }

    public static String prefixed(I18n i18n, CommandSender p, Object... args) {
        String pre = GetMeHome.getInstance().getPrefix();
        if (!pre.isEmpty()) pre += ' ';

        ChatColor focus = GetMeHome.getInstance().getFocusColor();
        ChatColor content = GetMeHome.getInstance().getContentColor();

        for (int i = args.length - 1; i >= 0; i--) {
            args[i] = focus + args[i].toString() + content;
        }

        return pre + content + base(i18n, getLocale(p), args);
    }

    public static String error(I18n i18n, CommandSender p, Object... args) {
        String pre = GetMeHome.getInstance().getPrefix();
        if (!pre.isEmpty()) pre += ' ';

        for (int i = args.length - 1; i >= 0; i--) {
            args[i] = args[i].toString();
        }

        return pre + ChatColor.RED + base(i18n, getLocale(p), args);
    }

    private static Locale getLocale(CommandSender sender) {
        if (!(sender instanceof Player))
            return Locale.getDefault();

        Player p = (Player) sender;

        Method method = null;
        for (Method m : p.getClass().getDeclaredMethods()) {
            if (m.getName().equals("getHandle"))
                method = m;
        }
        if (method == null) {
            return Locale.getDefault();
        }

        String locale;

        try {
            Object ep = method.invoke(p, (Object[]) null);
            Field f = ep.getClass().getDeclaredField("locale");
            f.setAccessible(true);
            locale = (String) f.get(ep);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
            return Locale.getDefault();
        }

        String[] l = locale.split("_", 3);

        if (l.length == 1)
            return new Locale(l[0]);

        if (l.length == 2)
            return new Locale(l[0],l[1]);

        return new Locale(l[0],l[1],l[2]);
    }
}
