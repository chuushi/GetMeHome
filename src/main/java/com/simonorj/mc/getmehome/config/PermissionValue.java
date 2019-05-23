package com.simonorj.mc.getmehome.config;

import jdk.nashorn.internal.ir.annotations.Immutable;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PermissionValue {
    private static final String VALUE_CHILD_NODE = "value";
    private static final String OPERATION_CHILD_NODE = "operation";
    private static final String WORLDS_CHILD_NODE = "worlds";

    public final String perm;
    public final Value[] vals;

    @Immutable
    public static class Value {
        public final int val;
        public final Operation oper;
        public final String[] worlds;

        private Value(int val) {
            this.val = val;
            this.oper = Operation.SET;
            this.worlds = null;
        }

        private Value(int val, Operation oper) {
            this.val = val;
            this.oper = oper;
            this.worlds = null;
        }

        private Value(int val, Operation oper, String[] worlds) {
            this.val = val;
            this.oper = oper;
            this.worlds = worlds;
        }
    }

    private PermissionValue(String perm, int val) {
        this.perm = perm;
        this.vals = new Value[]{new Value(val)};
    }

    private PermissionValue(String perm, int val, Operation oper) {
        this.perm = perm;
        this.vals = new Value[]{new Value(val, oper)};
    }

    private PermissionValue(String perm, Value[] vals) {
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

    static void parseConfigurationSection(List<PermissionValue> list, ConfigurationSection cs) {
        for (String perm : cs.getKeys(false)) {
            if (cs.isInt(perm)) {
                list.add(new PermissionValue(perm, cs.getInt(perm)));

            } else if (cs.contains(perm + "." + VALUE_CHILD_NODE)) {
                list.add(new PermissionValue(
                        perm,
                        cs.getInt(perm + "." + VALUE_CHILD_NODE),
                        Operation.fromString(cs.getString(perm + "." + OPERATION_CHILD_NODE, "set"))
                ));

            } else if (cs.isList(perm)) {
                List<Map<?, ?>> listVals = cs.getMapList(perm);

                // Convert from MapList to Value Array
                Value[] vals = new Value[listVals.size()];
                Arrays.setAll(vals, i -> {
                    Map<?, ?> m = listVals.get(i);

                    int val = (Integer) m.get(VALUE_CHILD_NODE);
                    String opString = (String) m.get(OPERATION_CHILD_NODE);
                    List wList = (List) m.get(WORLDS_CHILD_NODE);

                    String[] worlds = new String[wList.size()];
                    Arrays.setAll(worlds, j -> (String) wList.get(j));

                    return new Value(val, Operation.fromString(opString), worlds);
                });

                list.add(new PermissionValue(perm, vals));
            }
        }
    }
}
