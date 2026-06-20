package com.lexem.hexcodeevoke.hexitems;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class HexItemRegistery {

    private static final Map<String, String> hexItemList = new HashMap<>();

    private HexItemRegistery() {
    }

    public static void register(@Nonnull String blockId, @Nonnull String entityId) {
        if (hexItemList.containsKey(blockId)) {
            throw new IllegalArgumentException("duplicate blockId handler id: " + blockId);
        }
        if (hexItemList.containsValue(entityId)) {
            throw new IllegalArgumentException("duplicate entityId handler id: " + entityId);
        }
        hexItemList.put(blockId, entityId);
    }

    @Nullable
    public static Map.Entry<String, String> getByBlockId(@Nonnull String id) {
        return getAll().entrySet()
                .stream()
                .filter(entry -> Objects.equals(id, entry.getKey()))
                .findFirst()
                .orElse(null);
    }

    @Nonnull
    public static Map<String, String> getAll() {
        return Collections.unmodifiableMap(new HashMap<>(hexItemList));
    }

}
