package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.IPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionStoreItems;
import com.lexem.hexcodeevoke.utils.InventoryUtils;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionStoreItems extends ActionBase {

    public ActionStoreItems(@Nonnull BuilderActionStoreItems builderActionBase) {
        super(builderActionBase);
    }

    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull Role role, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, role, sensorInfo, dt, store);

        if (sensorInfo == null || !sensorInfo.hasPosition()) return false;

        IPositionProvider positionProvider = sensorInfo.getPositionProvider();
        if (positionProvider == null || !positionProvider.hasPosition()) return false;

        World world = store.getExternalData().getWorld();
        Vector3i chestPosition = new Vector3i(
                (int) Math.floor(positionProvider.getX()),
                (int) Math.floor(positionProvider.getY()),
                (int) Math.floor(positionProvider.getZ())
        );

        ChunkStore chunkStore = world.getChunkStore();
        Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(com.hypixel.hytale.math.util.ChunkUtil.indexChunkFromBlock(chestPosition.x, chestPosition.z));
        if (chunkRef == null || !chunkRef.isValid()) return false;

        BlockComponentChunk blockComponentChunk = chunkStore.getStore().getComponent(chunkRef, BlockComponentChunk.getComponentType());
        if (blockComponentChunk == null) return false;

        Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(ChunkUtil.indexBlockInColumn(chestPosition.x, chestPosition.y, chestPosition.z));
        if (blockRef == null) return false;

        ItemContainerBlock itemContainerBlock = chunkStore.getStore().getComponent(blockRef, ItemContainerBlock.getComponentType());
        if (itemContainerBlock == null) return false;

        SimpleItemContainer chestContainer = itemContainerBlock.getItemContainer();

        if (!InventoryUtils.canAddAnyItemToContainerNPC(chestContainer, npcRef, store)) {
            return false;
        }

        return InventoryUtils.transferItemsToChestNPC(npcRef, store, chestContainer);
    }
}