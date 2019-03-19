package com.simonorj.mc.getmehome;

import java.util.Locale;

public interface SpigotLocaleTool {
    static Locale parse(String locale) {
        String[] l = locale.split("_", 3);
        if (l.length == 1)
            return new Locale(l[0]);
        if (l.length == 2)
            return new Locale(l[0],l[1]);
        return new Locale(l[0],l[1],l[2]);
    }
}
