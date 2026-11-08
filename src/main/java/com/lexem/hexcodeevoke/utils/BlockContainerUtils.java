package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public final class BlockContainerUtils {

    private BlockContainerUtils() {
    }

    public static void transferToHexCreature(
            @Nonnull World world,
            @Nonnull Vector3i pos,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> npcRef,
            @Nonnull Vector3d dropPosition
    ) {
        Ref<ChunkStore> blockRef = BlockModule.getBlockEntity(world, pos.x, pos.y, pos.z);
        if (blockRef == null || !blockRef.isValid()) return;

        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        ItemContainerBlock containerBlock = chunkStore.getComponent(blockRef, ItemContainerBlock.getComponentType());
        if (containerBlock == null) {
            return;
        } else {
            containerBlock.getItemContainer();
        }

        List<ItemStack> items = containerBlock.getItemContainer().dropAllItemStacks();
        if (items.isEmpty()) return;

        InventoryComponent.Storage storage = store.getComponent(npcRef, InventoryComponent.Storage.getComponentType());
        ItemContainer inventory = storage != null ? storage.getInventory() : null;

        List<ItemStack> leftovers = new ArrayList<>();
        for (ItemStack stack : items) {
            if (inventory == null) {
                leftovers.add(stack);
                continue;
            }

            ItemStack remainder = inventory.addItemStack(stack).getRemainder();
            if (!ItemStack.isEmpty(remainder)) leftovers.add(remainder);
        }

        BlockModule.BlockStateInfo info = chunkStore.getComponent(blockRef, BlockModule.BlockStateInfo.getComponentType());
        if (info != null) info.markNeedsSaving(chunkStore);

        if (!leftovers.isEmpty()) {
            Holder<EntityStore>[] drops = ItemComponent.generateItemDrops(store, leftovers, dropPosition, Rotation3f.IDENTITY);
            store.addEntities(drops, AddReason.SPAWN);
        }
    }

    public static void storeHexCreatureItems(
            @Nonnull World world,
            @Nonnull Vector3i pos,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> npcRef
    ) {
        Ref<ChunkStore> blockRef = BlockUtils.getBlockEntityRef(world, pos, ItemContainerBlock.getComponentType());
        if (blockRef == null) return;

        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        ItemContainerBlock containerBlock = chunkStore.getComponent(blockRef, ItemContainerBlock.getComponentType());
        ItemContainer chest = containerBlock != null ? containerBlock.getItemContainer() : null;
        if (chest == null) return;

        CombinedItemContainer npcInventory = InventoryComponent.getCombined(store, npcRef, InventoryComponent.EVERYTHING);
        boolean moved = false;
        for (short slot = 0; slot < npcInventory.getCapacity(); slot++) {
            ItemStack stack = npcInventory.getItemStack(slot);
            if (ItemStack.isEmpty(stack)) continue;

            ItemStack remainder = chest.addItemStack(stack).getRemainder();
            if (ItemStack.isEmpty(remainder)) {
                npcInventory.removeItemStackFromSlot(slot);
                moved = true;
            } else if (remainder.getQuantity() < stack.getQuantity()) {
                npcInventory.setItemStackForSlot(slot, remainder);
                moved = true;
            }
        }

        if (!moved) return;
        BlockModule.BlockStateInfo info = chunkStore.getComponent(blockRef, BlockModule.BlockStateInfo.getComponentType());
        if (info != null) info.markNeedsSaving(chunkStore);
    }
}
