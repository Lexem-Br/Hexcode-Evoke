package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.ActiveSlotInventoryComponent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.util.InventoryHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public final class HeldItemUtils {

    private HeldItemUtils() {
    }

    public record HeldItem(@Nonnull ActiveSlotInventoryComponent component, short slot, @Nonnull ItemStack itemStack) {}

    @Nullable
    public static HeldItem findUsableHeldItem(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull List<String> items) {
        return findHeldItem(ref, store, items, false);
    }

    @Nullable
    public static HeldItem findBrokenHeldItem(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull List<String> items) {
        return findHeldItem(ref, store, items, true);
    }

    public static void decreaseDurability(@Nonnull HeldItem heldItem, double amount) {
        ItemStack itemStack = heldItem.itemStack();
        if (itemStack.isUnbreakable() || amount <= 0) return;

        replaceItem(heldItem, itemStack.withIncreasedDurability(-amount));
    }

    public static void replaceItem(@Nonnull HeldItem heldItem, @Nonnull ItemStack newItemStack) {
        ActiveSlotInventoryComponent component = heldItem.component();
        component.getInventory().replaceItemStackInSlot(heldItem.slot(), heldItem.itemStack(), newItemStack);

        if (component instanceof InventoryComponent.Hotbar hotbar) {
            hotbar.setOutdatedEquipment(true);
        } else if (component instanceof InventoryComponent.Utility utility) {
            utility.setOutdatedEquipment(true);
        }
    }

    @Nullable
    private static HeldItem findHeldItem(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull List<String> items, boolean broken) {
        HeldItem mainHand = getHeldItem(store.getComponent(ref, InventoryComponent.Hotbar.getComponentType()), items, broken);
        if (mainHand != null) return mainHand;

        return getHeldItem(store.getComponent(ref, InventoryComponent.Utility.getComponentType()), items, broken);
    }

    @Nullable
    private static HeldItem getHeldItem(@Nullable ActiveSlotInventoryComponent component, @Nonnull List<String> items, boolean broken) {
        if (component == null) return null;

        ItemStack itemStack = component.getActiveItem();
        if (ItemStack.isEmpty(itemStack) || itemStack.isBroken() != broken || !InventoryHelper.matchesItem(items, itemStack)) return null;

        return new HeldItem(component, component.getActiveSlot(), itemStack);
    }
}
