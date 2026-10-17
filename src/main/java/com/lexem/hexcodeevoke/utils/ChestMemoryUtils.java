package com.lexem.hexcodeevoke.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.sensorinfo.IPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import org.joml.Vector3d;

public class ChestMemoryUtils {

    public ChestMemoryUtils() {}

    public static void registerChestInMemory(InfoProvider sensorInfo, Store<EntityStore> store, HexCreatureComponent hexCreatureComponent) {
        if (sensorInfo == null || hexCreatureComponent == null) {
            return;
        }

        IPositionProvider positionProvider = sensorInfo.getPositionProvider();
        if (positionProvider == null || !positionProvider.hasPosition()) {
            return;
        }

        World world = store.getExternalData().getWorld();
        Vector3d chestPosition = new Vector3d(
                Math.floor(positionProvider.getX()),
                Math.floor(positionProvider.getY()),
                Math.floor(positionProvider.getZ())
        );

        ChestLookup lookup = findChest(world, chestPosition);
        if (lookup.missing()) {
            hexCreatureComponent.forgetChest(chestPosition);
            return;
        }
        if (lookup.container() == null) return;

        String[] differentItemsOnChest = InventoryUtils.getItemIdsFromContainer(lookup.container());
        hexCreatureComponent.updateChestMemory(chestPosition, differentItemsOnChest);
    }

    public static ChestLookup findChest(World world, Vector3d chestPosition) {
        int x = (int) Math.floor(chestPosition.x);
        int y = (int) Math.floor(chestPosition.y);
        int z = (int) Math.floor(chestPosition.z);

        ChunkStore chunkStore = world.getChunkStore();
        Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(ChunkUtil.indexChunkFromBlock(x, z));
        if (chunkRef == null || !chunkRef.isValid()) return ChestLookup.UNKNOWN;

        Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReferenceAtBlock(x, y, z);
        if (sectionRef == null || !sectionRef.isValid()) return ChestLookup.UNKNOWN;

        Store<ChunkStore> chunkComponentStore = chunkStore.getStore();
        Ref<ChunkStore> blockRef = BlockModule.getBlockEntity(chunkComponentStore, sectionRef, x, y, z);
        if (blockRef == null) return ChestLookup.MISSING;

        ItemContainerBlock itemContainerBlock = chunkComponentStore.getComponent(blockRef, ItemContainerBlock.getComponentType());
        if (itemContainerBlock == null) return ChestLookup.MISSING;

        return new ChestLookup(false, itemContainerBlock.getItemContainer());
    }

    public record ChestLookup(boolean missing, SimpleItemContainer container) {
        public static final ChestLookup UNKNOWN = new ChestLookup(false, null);
        public static final ChestLookup MISSING = new ChestLookup(true, null);
    }
}
