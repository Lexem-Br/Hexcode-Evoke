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
        long chunkIndex = ChunkUtil.indexChunkFromBlock(positionProvider.getX(), positionProvider.getZ());
        Ref<ChunkStore> chunkRef = world.getChunkStore().getChunkReference(chunkIndex);
        if (chunkRef == null) return;

        Vector3d chestPosition = new Vector3d(
                Math.floor(positionProvider.getX()),
                Math.floor(positionProvider.getY()),
                Math.floor(positionProvider.getZ())
        );

        Store<ChunkStore> chunkComponentStore = world.getChunkStore().getStore();
        ChunkStore chunkStore = world.getChunkStore();
        Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReferenceAtBlock((int)chestPosition.x, (int)chestPosition.y, (int)chestPosition.z);
        if (sectionRef == null) return;

        Ref<ChunkStore> blockRef = BlockModule.getBlockEntity(chunkComponentStore, sectionRef, (int)chestPosition.x, (int)chestPosition.y, (int)chestPosition.z);
        if (blockRef == null) return;

        ItemContainerBlock itemContainerBlock = chunkComponentStore.getComponent(blockRef, ItemContainerBlock.getComponentType());
        if (itemContainerBlock == null) return;

        SimpleItemContainer chestContainer = itemContainerBlock.getItemContainer();
        String[] differentItemsOnChest = InventoryUtils.getItemIdsFromContainer(chestContainer);

        hexCreatureComponent.updateChestMemory(chestPosition, differentItemsOnChest);
    }

}
