package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.builtin.adventure.farming.states.TilledSoilBlock;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockEntity;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3i;

import javax.annotation.Nonnull;

public final class FertilizerUtils {
    public static final String FERTILIZER_ITEM_ID = "Tool_Fertilizer";

    private FertilizerUtils() {
    }

    public static boolean isFertilizableSoil(@Nonnull Vector3i pos, @Nonnull World world) {
        BlockType soilType = BlockUtils.getBlockTypeByPosition(pos, world);
        if (soilType == null || !BlockEntity.declaresComponent(soilType, TilledSoilBlock.getComponentType())) return false;

        BlockType cropType = BlockUtils.getBlockTypeByPosition(new Vector3i(pos.x, pos.y + 1, pos.z), world);
        if (cropType == null || cropType.getFarming() == null || cropType.getFarming().getStages() == null) return false;

        Ref<ChunkStore> blockRef = BlockUtils.getBlockEntityRef(world, pos, null);
        TilledSoilBlock soil = blockRef != null ? world.getChunkStore().getStore().getComponent(blockRef, TilledSoilBlock.getComponentType()) : null;
        return soil == null || !soil.isFertilized();
    }

    public static boolean fertilize(@Nonnull Vector3i pos, @Nonnull World world) {
        if (!isFertilizableSoil(pos, world)) return false;

        Ref<ChunkStore> blockRef = BlockUtils.getBlockEntityRef(world, pos, TilledSoilBlock.getComponentType());
        if (blockRef == null) return false;

        Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
        TilledSoilBlock soil = chunkStore.getComponent(blockRef, TilledSoilBlock.getComponentType());
        if (soil == null) return false;

        soil.setFertilized(true);
        BlockModule.BlockStateInfo info = chunkStore.getComponent(blockRef, BlockModule.BlockStateInfo.getComponentType());
        if (info != null) info.markNeedsSaving(chunkStore);

        setTicking(world, pos.x, pos.y, pos.z);
        setTicking(world, pos.x, pos.y + 1, pos.z);
        return true;
    }

    public static boolean isHoldingFertilizer(@Nonnull Ref<EntityStore> npcRef, @Nonnull Store<EntityStore> store) {
        ItemStack itemInHand = InventoryComponent.getItemInHand(store, npcRef);
        return !ItemStack.isEmpty(itemInHand) && FERTILIZER_ITEM_ID.equals(itemInHand.getItemId());
    }

    public static void consumeFertilizer(@Nonnull Ref<EntityStore> npcRef, @Nonnull Store<EntityStore> store) {
        InventoryComponent.Hotbar hotbar = store.getComponent(npcRef, InventoryComponent.Hotbar.getComponentType());
        if (hotbar == null) return;

        ItemContainer inventory = hotbar.getInventory();
        byte activeSlot = hotbar.getActiveSlot();
        short slot = activeSlot >= 0 && activeSlot < inventory.getCapacity() ? activeSlot : 0;

        ItemStack fertilizer = inventory.getItemStack(slot);
        if (ItemStack.isEmpty(fertilizer) || !FERTILIZER_ITEM_ID.equals(fertilizer.getItemId())) return;

        if (fertilizer.getMaxDurability() > 0) {
            ItemStack used = fertilizer.withIncreasedDurability(-1);
            if (used.getDurability() <= 0) {
                inventory.removeItemStackFromSlot(slot);
            } else {
                inventory.setItemStackForSlot(slot, used);
            }
        } else if (fertilizer.getQuantity() > 1) {
            inventory.setItemStackForSlot(slot, fertilizer.withQuantity(fertilizer.getQuantity() - 1));
        } else {
            inventory.removeItemStackFromSlot(slot);
        }
        hotbar.setOutdatedEquipment(true);
    }

    private static void setTicking(@Nonnull World world, int x, int y, int z) {
        Ref<ChunkStore> section = world.getChunkStore().getChunkSectionReferenceAtBlock(x, y, z);
        if (section == null || !section.isValid()) return;

        BlockSection blockSection = world.getChunkStore().getStore().getComponent(section, BlockSection.getComponentType());
        if (blockSection != null) blockSection.setTicking(x, y, z, true);
    }
}
