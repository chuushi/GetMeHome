package com.simonorj.mc.getmehome.config;

import com.google.common.collect.ImmutableList;
import com.simonorj.mc.getmehome.GetMeHome;
import jdk.nashorn.internal.ir.annotations.Immutable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Immutable
public class PermValue {
    private static final String VALUE_LIST_NODE = "value";
    private static final String OPERATION_LIST_NODE = "operation";
    private static final String WORLDS_LIST_NODE = "worlds";
    public static final String PERM_LIST_NODE = "perm";

    public final String perm;
    public final int val;
    public final Operation oper;
    public final List<String> worlds;

    private PermValue(String perm, int val, Operation oper, List<String> worlds) {
        this.perm = perm;
        this.val = val;
        this.oper = oper;
        this.worlds = worlds;
    }

    enum Operation {
        SET, WORLD, ADD, SUB;

        private static Operation fromString(String input) {
            if (input == null)
                return SET;

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

    static List<PermValue> parseList(List<Map<?, ?>> mapList) {
        ArrayList<PermValue> ret = new ArrayList<>();

        for (Map<?, ?> l : mapList) {
            try {
                ret.add(parsePermissionGroup(l));
            } catch (ClassCastException e) {
                Object p = l.get("perm");
                if (!(p instanceof String)) {
                    GetMeHome.getInstance().getLogger().warning("Failed to parse 'perm' to String");
                } else {
                    String perm = (String) p;
                    if (!(l.get("value") instanceof Integer)) {
                        GetMeHome.getInstance().getLogger().warning("Failed to parse 'value' of " + perm + " to String");
                    }
                }
            }
        }
        return ret;
    }

    static PermValue parsePermissionGroup(Map<?, ?> m) throws ClassCastException {
        String perm = (String) m.get(PERM_LIST_NODE);
        int value = (Integer) m.get(VALUE_LIST_NODE);
        String opString = (String) m.get(OPERATION_LIST_NODE);
        List wList = (List) m.get(WORLDS_LIST_NODE);

        // Pares Operation
        Operation oper = Operation.fromString(opString);

        // Parse worlds
        List<String> worlds;
        if (wList != null) {
            int size = wList.size();
            if (size == 0) {
                worlds = null;
            } else {
                ImmutableList.Builder<String> worldsBuilder = ImmutableList.builder();
                for (Object o : wList) {
                    worldsBuilder.add(((String) o).toLowerCase());
                }
                worlds = worldsBuilder.build();
            }
        } else {
            worlds = null;
        }

        return new PermValue(perm, value, oper, worlds);
    }
}
