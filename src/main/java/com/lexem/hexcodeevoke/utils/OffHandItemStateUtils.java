package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class OffHandItemStateUtils {
    private static final Set<String> TRANSIENT_ITEMS = ConcurrentHashMap.newKeySet();

    private OffHandItemStateUtils() {
    }

    public static void registerTransientItems(@Nullable String[] items) {
        if (items != null) Collections.addAll(TRANSIENT_ITEMS, items);
    }

    public static boolean matches(@Nonnull String itemId, @Nullable ItemStack itemStack) {
        if (ItemStack.isEmpty(itemStack)) return false;
        if (itemId.equals(itemStack.getItemId())) return true;

        Item item = Item.getAssetMap().getAsset(itemId);
        return item != null && item.getStateForItem(itemStack.getItemId()) != null;
    }

    @Nullable
    public static ItemStack setState(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> accessor, @Nonnull Iterable<String> items, @Nullable String state) {
        InventoryComponent.Utility utility = accessor.getComponent(ref, InventoryComponent.Utility.getComponentType());
        if (utility == null) return null;

        return setState(utility, items, state);
    }

    public static boolean hasTransientState(@Nonnull InventoryComponent.Utility utility) {
        ItemStack itemStack = utility.getActiveItem();
        if (ItemStack.isEmpty(itemStack)) return false;

        for (String itemId : TRANSIENT_ITEMS) {
            if (!itemId.equals(itemStack.getItemId()) && matches(itemId, itemStack)) return true;
        }
        return false;
    }

    public static void resetTransientState(@Nonnull InventoryComponent.Utility utility) {
        setState(utility, TRANSIENT_ITEMS, null);
    }

    @Nullable
    private static ItemStack setState(@Nonnull InventoryComponent.Utility utility, @Nonnull Iterable<String> items, @Nullable String state) {
        ItemStack itemStack = utility.getActiveItem();
        if (ItemStack.isEmpty(itemStack)) return null;

        for (String itemId : items) {
            if (!matches(itemId, itemStack)) continue;

            String targetId = itemId;
            if (state != null) {
                Item item = Item.getAssetMap().getAsset(itemId);
                targetId = item != null ? item.getItemIdForState(state) : null;
                if (targetId == null) return null;
            }
            if (targetId.equals(itemStack.getItemId())) return null;

            ItemStack newItemStack = new ItemStack(targetId, itemStack.getQuantity(), itemStack.getDurability(), itemStack.getMaxDurability(), itemStack.getMetadata());
            HeldItemUtils.replaceItem(new HeldItemUtils.HeldItem(utility, utility.getActiveSlot(), itemStack), newItemStack);
            return newItemStack;
        }
        return null;
    }
}
