package com.simonorj.mc.getmehome;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.ResourceBundle;

public class MessageTool {
    private static String base(String i18n, Locale locale, Object... args) {
        String msg = ResourceBundle.getBundle("i18n.GetMeHome", locale).getString(i18n);

        return String.format(msg, args);
    }

    public static String raw(String i18n, CommandSender p, Object... args) {
        return base(i18n, getLocale(p), args);
    }

    public static String prefixed(String i18n, CommandSender p, Object... args) {
        String pre = GetMeHome.getInstance().getPrefix();
        if (!pre.isEmpty()) pre += ' ';

        ChatColor focus = GetMeHome.getInstance().getFocusColor();
        ChatColor content = GetMeHome.getInstance().getContentColor();

        for (int i = args.length - 1; i >= 0; i--) {
            args[i] = focus + args[i].toString() + content;
        }

        return pre + content + base(i18n, getLocale(p), args);
    }

    public static String error(String i18n, CommandSender p, Object... args) {
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
