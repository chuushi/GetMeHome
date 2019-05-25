package com.simonorj.mc.getmehome.config;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PermValue {
    private static final String VALUE_CHILD_NODE = "value";
    private static final String OPERATION_CHILD_NODE = "operation";
    private static final String WORLDS_CHILD_NODE = "worlds";

    public final String perm;
    public final Value[] vals;

    @Immutable
    public static class Value {
        public final int val;
        public final Operation oper;
        public final List<String> worlds;

        private Value(int val) {
            this(val, Operation.SET, null);
        }

        private Value(int val, Operation oper, List<String> worlds) {
            this.val = val;
            this.oper = oper;
            this.worlds = worlds;
        }
    }

    private PermValue(String perm, Value val) {
        this.perm = perm;
        this.vals = new Value[]{val};
    }

    private PermValue(String perm, Value[] vals) {
        this.perm = perm;
        this.vals = vals;
    }

    enum Operation {
        SET, WORLD, ADD, SUB;

        private static Operation fromString(String input) {
            switch (input.toLowerCase()) {
                case "set":
                    return SET;
                case "world":
                case "worlds":
                    return WORLD;
                case "add":
                case "plus":
                    return ADD;
                case "sub":
                case "subtract":
                case "minus":
                    return SUB;
            }
            return SET;
        }
    }

    static List<PermValue> toList(ConfigurationSection cs) {
        ArrayList<PermValue> ret = new ArrayList<>();
        for (String perm : cs.getKeys(true)) {
            if (!(cs.isInt(perm) && perm.endsWith('.' + VALUE_CHILD_NODE))
                    && !cs.isList(perm)
                    && !cs.isInt(perm + '.' + VALUE_CHILD_NODE)
            ) continue;

            ret.add(parsePermissionGroup(perm, cs));
        }
        return ret;
    }

    static PermValue parsePermissionGroup(String perm, ConfigurationSection cs) {
        if (cs.isInt(perm))
            return new PermValue(perm, new Value(cs.getInt(perm)));
        else if (cs.isInt(perm + "." + VALUE_CHILD_NODE))
            return new PermValue(perm, parseValue(cs.getConfigurationSection(perm)));
        else if (cs.isList(perm))
            return new PermValue(perm, parseValues(cs.getMapList(perm)));
        else
            return new PermValue(perm, new Value(1));
    }

    static PermValue.Value[] parseValues(List<Map<?, ?>> configurationSectionList) {
        // Convert from MapList to Value Array
        Value[] ret = new Value[configurationSectionList.size()];
        Arrays.setAll(ret, i -> parseValue(configurationSectionList.get(i)));
        return ret;
    }

    static PermValue.Value parseValue(ConfigurationSection configurationSection) {
        return parseValue(configurationSection.getValues(false));
    }

    static PermValue.Value parseValue(Map<?, ?> configurationSection) {
        if (!configurationSection.containsKey(VALUE_CHILD_NODE))
            return null;

        int val = (Integer) configurationSection.get(VALUE_CHILD_NODE);
        String opString = (String) configurationSection.get(OPERATION_CHILD_NODE);
        List wList = (List) configurationSection.get(WORLDS_CHILD_NODE);

        String[] worlds;
        if (wList != null) {
            int size = wList.size();
            if (size == 0) {
                worlds = null;
            } else {
                worlds = new String[size];
                Arrays.setAll(worlds, j -> ((String) wList.get(j)).toLowerCase());
            }
        } else {
            worlds = null;
        }

        return new Value(
                val,
                opString == null ? null : Operation.fromString(opString),
                worlds == null ? null : Arrays.asList(worlds)
        );
    }
}
