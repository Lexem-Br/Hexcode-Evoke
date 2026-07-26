package com.lexem.hexcodeevoke.hexitems;

import com.hypixel.hytale.logger.HytaleLogger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class HexItemRegistery {

    private static final Map<String, String> hexItemList = new HashMap<>();
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static boolean firstRun = true;


    private HexItemRegistery() {
    }

    @Nullable
    public static Map.Entry<String, String> getByBlockId(@Nonnull String id) {
        return getAll().entrySet()
                .stream()
                .filter(entry -> Objects.equals(id, entry.getKey()))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public static Map.Entry<String, String> getByEntityId(@Nonnull String id) {
        return getAll().entrySet()
                .stream()
                .filter(entry -> Objects.equals(id, entry.getValue()))
                .findFirst()
                .orElse(null);
    }

    @Nonnull
    public static String findBlockIdByEntityId(String entityId) {
        Map.Entry<String, String> entry = getByEntityId(entityId);
        return entry != null ? entry.getKey() : "";
    }

    @Nonnull
    public static String findEntityIdByBlockId(String blockId) {
        Map.Entry<String, String> entry = getByBlockId(blockId);
        return entry != null ? entry.getValue() : "";
    }

    public static boolean isHexCreature(@Nonnull String entityId) {
        return getByEntityId(entityId) != null;
    }

    @Nonnull
    public static Map<String, String> getAll() {
        if (firstRun) {
            AllowedHexItemsAsset allowedHexItems = AllowedHexItemsAsset.getAssetMap().getAsset("AllowedHexItems");
            assert allowedHexItems != null;

            AllowedHexItemsAsset.HexItem[] hexItems = allowedHexItems.getHexItems();
            for (AllowedHexItemsAsset.HexItem hexItem : hexItems) {
                if (!hexItemList.containsKey(hexItem.blockId) && !hexItemList.containsValue(hexItem.entityId)) {
                    hexItemList.put(hexItem.blockId, hexItem.entityId);
                }
            }

            LOGGER.atSevere().log("Funcionou!");

            firstRun = false;
        }

        return new HashMap<>(hexItemList);
    }

    @Nonnull
    public static ArrayList<String> getAllBlocks(){ return new ArrayList<>(getAll().keySet()); }
}
