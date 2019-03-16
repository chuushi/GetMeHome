package com.simonorj.mc.getmehome;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

import java.util.Locale;
import java.util.ResourceBundle;

public interface MessagePreparer {
    default BaseComponent base(String i18n, Locale locale, Object... args) {
        String msg = ResourceBundle.getBundle("i18n.GetMeHome").getString(i18n);
        BaseComponent ret;

        if (args.length == 0)
            ret = new TextComponent(msg);
        else
            ret = new TranslatableComponent(msg, args);

        return ret;
    }

    default BaseComponent regular(String i18n, Locale locale, Object... args) {
        BaseComponent ret = base(i18n, locale, args);

        // TODO: Customizable color
        ret.setColor(ChatColor.YELLOW);

        return ret;
    }

    default BaseComponent error(String i18n, Locale locale, Object... args) {
        BaseComponent ret = base(i18n, locale, args);

        ret.setColor(ChatColor.RED);

        return ret;
    }
}
