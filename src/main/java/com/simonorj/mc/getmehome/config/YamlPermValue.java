package com.simonorj.mc.getmehome.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class YamlPermValue {
    private final List<PermValue> limit;
    private final int defaultValue;

    public YamlPermValue(YamlConfiguration config, String sectionName) {
        this.limit = PermValue.parseList(config.getMapList(sectionName));
        this.defaultValue = config.getInt("default." + sectionName);
    }

    private int noNeg(int n) {
        return n < 0 ? 0 : n;
    }

    public WorldValue calcFor(Player p) {
        int ret = 0;
        String w = p.getWorld().getName().toLowerCase();
        List<WorldValue> deducts = new ArrayList<>();

        for (PermValue l : limit) {
            if (!p.hasPermission(l.perm))
                continue;

            if (l.worlds != null && !l.worlds.contains(w)) {
                if (l.oper == PermValue.Operation.SUB) {
                    deducts.add(new WorldValue(l.worlds, -l.val));
                } else {
                    deducts.add(new WorldValue(l.worlds, l.val));
                }
                continue;
            }

            // This matches
            switch (l.oper) {
                case SET:
                    if (l.val == -1)
                        return new WorldValue(null, -1);
                    return new WorldValue(null, noNeg(ret + l.val), deducts);
                case WORLD:
                    if (l.val == -1)
                        return new WorldValue(l.worlds, -1);
                    return new WorldValue(l.worlds, noNeg(ret + l.val), deducts);
                case ADD:
                    ret += l.val;
                    break;
                case SUB:
                    ret -= l.val;
            }
            break;
        }

        if (defaultValue == -1)
            return new WorldValue(null, -1);
        return new WorldValue(null, noNeg(ret + defaultValue), deducts);
    }

    public class WorldValue {
        public final List<String> worlds;
        public int value;
        public final List<WorldValue> deducts;

        private WorldValue(List<String> worlds, int value) {
            this(worlds, value, null);
        }

        private WorldValue(List<String> worlds, int value, List<WorldValue> deducts) {
            this.worlds = worlds;
            this.value = value;
            this.deducts = deducts;
        }
    }
}
