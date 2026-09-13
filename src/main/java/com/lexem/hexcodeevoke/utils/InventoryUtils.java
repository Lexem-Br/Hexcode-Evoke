package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.*;

public class InventoryUtils {

    private InventoryUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static boolean canAddAnyItemToContainerNPC(SimpleItemContainer chestContainer, Ref<EntityStore> npcRef, Store<EntityStore> store, boolean skipHotbarSlotZero) {
        if (chestContainer == null) return false;

        CombinedItemContainer combinedContainer = InventoryComponent.getCombined(
                store,
                npcRef,
                InventoryComponent.Storage.getComponentType(),
                InventoryComponent.Hotbar.getComponentType()
        );

        InventoryComponent.Storage storageComponent = store.getComponent(npcRef, InventoryComponent.Storage.getComponentType());
        short hotbarSlotZeroIndex = -1;

        if (skipHotbarSlotZero && storageComponent != null) {
            hotbarSlotZeroIndex = storageComponent.getInventory().getCapacity();
        }

        for (short slot = 0; slot < combinedContainer.getCapacity(); slot++) {
            if (skipHotbarSlotZero && slot == hotbarSlotZeroIndex) {
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

    public static boolean transferItemsToChestNPC(Ref<EntityStore> npcRef, Store<EntityStore> store, SimpleItemContainer chestContainer, boolean skipHotbarSlotZero) {
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

        short hotbarSlotZeroIndex = -1;
        if (skipHotbarSlotZero) {
            hotbarSlotZeroIndex = storageComponent.getInventory().getCapacity();
        }

        for (short slot = 0; slot < npcCombinedContainer.getCapacity(); slot++) {
            if (skipHotbarSlotZero && slot == hotbarSlotZeroIndex) {
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

    public static String[] getItemIdsFromContainer(ItemContainer container) {
        if (container == null) {
            return new String[0];
        }

        Set<String> itemIds = new HashSet<>();

        for (short slot = 0; slot < container.getCapacity(); slot++) {
            ItemStack itemStack = container.getItemStack(slot);
            if (!ItemStack.isEmpty(itemStack)) {
                Item item = itemStack.getItem();
                String itemId = item.getId();
                if (itemId != null && !itemId.isEmpty()) {
                    itemIds.add(itemId);
                }
            }
        }

        return itemIds.toArray(new String[0]);
    }

    public static boolean containsItemId(ItemContainer container, String itemId) {
        if (container == null || itemId == null || itemId.isEmpty()) {
            return false;
        }

        for (short slot = 0; slot < container.getCapacity(); slot++) {
            ItemStack itemStack = container.getItemStack(slot);
            if (itemStack != null && !itemStack.isEmpty()) {
                Item item = itemStack.getItem();
                if (itemId.equals(item.getId())) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean canAddAnyQuantityOfItem(SimpleItemContainer chestContainer, String itemId) {
        if (chestContainer == null || itemId == null || itemId.isEmpty()) {
            return false;
        }

        ItemStack tempItemStack = new ItemStack(itemId, 1);
        tempItemStack.getItem();

        for (short slot = 0; slot < chestContainer.getCapacity(); slot++) {
            if (chestContainer.canAddItemStackToSlot(slot, tempItemStack, false, false)) {
                return true;
            }
        }

        return false;
    }

    public static boolean transferAllItemsOfType(CombinedItemContainer fromContainer, CombinedItemContainer toContainer, String itemId, int skipSlot) {
        if (fromContainer == null || toContainer == null || itemId == null || itemId.isEmpty()) {
            return false;
        }

        boolean itemTransferred = false;

        for (short fromSlot = 0; fromSlot < fromContainer.getCapacity(); fromSlot++) {
            ItemStack itemStack = fromContainer.getItemStack(fromSlot);

            if (!ItemStack.isEmpty(itemStack) && itemId.equals(itemStack.getItem().getId())) {
                for (short toSlot = 0; toSlot < toContainer.getCapacity(); toSlot++) {
                    if (skipSlot >= 0 && toSlot == skipSlot) {
                        continue;
                    }

                    if (toContainer.canAddItemStackToSlot(toSlot, itemStack, false, false)) {
                        ItemStack removedItem = fromContainer.removeItemStackFromSlot(fromSlot).getSlotBefore();

                        if (removedItem != null && !removedItem.isEmpty()) {
                            var addResult = toContainer.addItemStackToSlot(toSlot, removedItem, false, false);

                            if (addResult.succeeded()) {
                                ItemStack remainder = addResult.getRemainder();
                                if (remainder != null && !remainder.isEmpty()) {
                                    fromContainer.addItemStackToSlot(fromSlot, remainder, false, false);
                                }
                                itemTransferred = true;
                            } else {
                                fromContainer.addItemStackToSlot(fromSlot, removedItem, false, false);
                            }
                            break;
                        }
                    }
                }
            }
        }

        return itemTransferred;
    }

    public static boolean canReturnAnyItemToOwner(CombinedItemContainer minionContainer, CombinedItemContainer ownerContainer) {
        if (minionContainer == null || ownerContainer == null) {
            return false;
        }

        for (short slot = 0; slot < minionContainer.getCapacity(); slot++) {
            ItemStack itemStack = minionContainer.getItemStack(slot);
            if (!ItemStack.isEmpty(itemStack)) {
                String itemId = itemStack.getItem().getId();
                if (itemId != null && !itemId.isEmpty()) {
                    if (canReturnItemsToOwner(minionContainer, ownerContainer, itemId)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean canReturnItemsToOwner(CombinedItemContainer minionContainer, CombinedItemContainer ownerContainer, String itemId) {
        if (minionContainer == null || ownerContainer == null || itemId == null || itemId.isEmpty()) {
            return false;
        }

        boolean hasItem = false;
        for (short slot = 0; slot < minionContainer.getCapacity(); slot++) {
            ItemStack itemStack = minionContainer.getItemStack(slot);
            if (!ItemStack.isEmpty(itemStack) && itemId.equals(itemStack.getItem().getId())) {
                hasItem = true;
                break;
            }
        }

        if (!hasItem) {
            return false;
        }

        ItemStack tempItemStack = new ItemStack(itemId, 1);
        if (tempItemStack.getItem() == null) {
            return false;
        }

        for (short slot = 0; slot < ownerContainer.getCapacity(); slot++) {
            if (ownerContainer.canAddItemStackToSlot(slot, tempItemStack, false, false)) {
                return true;
            }
        }

        return false;
    }
}