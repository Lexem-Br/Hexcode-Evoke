package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class InventoryUtils {

    private InventoryUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static boolean canAddAnyItemToContainerNPC(SimpleItemContainer chestContainer, Ref<EntityStore> npcRef, Store<EntityStore> store) {
        if (chestContainer == null) return false;

        CombinedItemContainer combinedContainer = InventoryComponent.getCombined(
                store,
                npcRef,
                InventoryComponent.Storage.getComponentType(),
                InventoryComponent.Hotbar.getComponentType()
        );

        InventoryComponent.Storage storageComponent = store.getComponent(npcRef, InventoryComponent.Storage.getComponentType());
        if (storageComponent == null) return false;

        short hotbarSlotZeroIndex = storageComponent.getInventory().getCapacity(); // Primeiro slot da hotbar no combined container

        for (short slot = 0; slot < combinedContainer.getCapacity(); slot++) {
            if (slot == hotbarSlotZeroIndex) {
                continue;
            }

            ItemStack npcItemStack = combinedContainer.getItemStack(slot);

            if (!ItemStack.isEmpty(npcItemStack)) {
                for (short chestSlot = 0; chestSlot < chestContainer.getCapacity(); chestSlot++) {
                    if (chestContainer.canAddItemStackToSlot(chestSlot, npcItemStack, false, false)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean canAddAnyItemToContainer(SimpleItemContainer targetContainer, ItemContainer entityContainer) {
        if (targetContainer == null || entityContainer == null) {
            return false;
        }

        for (short entitySlot = 0; entitySlot < entityContainer.getCapacity(); entitySlot++) {
            ItemStack npcItemStack = entityContainer.getItemStack(entitySlot);

            if (!ItemStack.isEmpty(npcItemStack)) {
                for (short chestSlot = 0; chestSlot < targetContainer.getCapacity(); chestSlot++) {
                    if (targetContainer.canAddItemStackToSlot(chestSlot, npcItemStack, false, false)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean transferItemsToChestNPC(Ref<EntityStore> npcRef, Store<EntityStore> store, SimpleItemContainer chestContainer) {
        boolean anyItemTransferred = false;

        CombinedItemContainer npcCombinedContainer = InventoryComponent.getCombined(
                store,
                npcRef,
                InventoryComponent.Storage.getComponentType(),
                InventoryComponent.Hotbar.getComponentType()
        );

        InventoryComponent.Storage storageComponent = store.getComponent(npcRef, InventoryComponent.Storage.getComponentType());
        if (storageComponent == null) {
            return false;
        }

        short hotbarSlotZeroIndex = storageComponent.getInventory().getCapacity();
        for (short slot = 0; slot < npcCombinedContainer.getCapacity(); slot++) {
            if (slot == hotbarSlotZeroIndex) {
                continue;
            }

            ItemStack npcItemStack = npcCombinedContainer.getItemStack(slot);
            if (ItemStack.isEmpty(npcItemStack)) {
                continue;
            }

            var addResult = chestContainer.addItemStack(npcItemStack, false, false, false);

            if (addResult.succeeded()) {
                ItemStack remainder = addResult.getRemainder();
                int originalQuantity = npcItemStack.getQuantity();
                int transferred = originalQuantity - (remainder != null ? remainder.getQuantity() : 0);

                if (transferred > 0) {
                    anyItemTransferred = true;

                    if (remainder == null || remainder.getQuantity() == 0) {
                        npcCombinedContainer.removeItemStackFromSlot(slot);
                    } else {
                        npcCombinedContainer.setItemStackForSlot(slot, remainder);
                    }
                }
            }
        }

        return anyItemTransferred;
    }
}