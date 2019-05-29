package com.simonorj.mc.getmehome.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class YamlPermValue {
    private final YamlConfiguration config;
    private final List<PermValue> limit;
    private final PermValue defaultValue;

    public YamlPermValue(YamlConfiguration config, String sectionName) {
        this.config = config;
        this.limit = PermValue.toList(config.getConfigurationSection(sectionName));
        this.defaultValue = PermValue.parsePermissionGroup(sectionName, config.getConfigurationSection("default"));
    }

    public YamlConfiguration getConfig() {
        return config;
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

            for (PermValue.Value v : l.vals) {
                if (v.worlds != null && !v.worlds.contains(w)) {
                    if (v.oper != PermValue.Operation.SUB)
                        deducts.add(new WorldValue(v.worlds, v.val));
                    continue;
                }

                // This matches
                switch (v.oper) {
                    case SET:
                        if (v.val == -1)
                            return new WorldValue(null, -1);
                        return new WorldValue(null, noNeg(ret + v.val), deducts);
                    case WORLD:
                        if (v.val == -1)
                            return new WorldValue(v.worlds, -1);
                        return new WorldValue(v.worlds, noNeg(ret + v.val), deducts);
                    case ADD:
                        ret += v.val;
                        break;
                    case SUB:
                        ret -= v.val;
                }
                break;
            }
        }

        for (PermValue.Value v : defaultValue.vals) {
            if (v.worlds != null && !v.worlds.contains(w))
                continue;

            if (v.oper == PermValue.Operation.WORLD) {
                if (v.val == -1)
                    return new WorldValue(v.worlds, -1);
                return new WorldValue(v.worlds, noNeg(ret + v.val), deducts);
            }

            if (v.val == -1)
                return new WorldValue(null, -1);
            return new WorldValue(null, noNeg(ret + v.val), deducts);
        }

        // This function will never get here but compiler requires a return statement at the end :I
        return new WorldValue(null, 0, deducts);
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
