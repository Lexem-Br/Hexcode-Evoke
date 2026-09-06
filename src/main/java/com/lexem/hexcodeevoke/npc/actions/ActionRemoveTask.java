package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.IPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.components.ChestMemoryComponent;
import com.lexem.hexcodeevoke.components.ChestTaskComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderRemoveMinionTask;
import com.lexem.hexcodeevoke.utils.InventoryUtils;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class ActionRemoveTask extends ActionBase {

    public ActionRemoveTask(@Nonnull BuilderRemoveMinionTask builder) {
      super(builder);
   }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, executionSupport, sensorInfo, dt, store);

        HexCreatureMinionComponent minionComponent = store.getComponent(npcRef, HexCreatureMinionComponent.getComponentType());
        if (minionComponent == null) return false;

        UUID uuid = UUID.fromString(minionComponent.getOwnerUUID());
        Ref<EntityStore> hcRef = store.getExternalData().getRefFromUUID(uuid);
        if (hcRef == null) return false;

        HexCreatureComponent hexCreatureComponent = store.getComponent(hcRef, HexCreatureComponent.getComponentType());
        if (hexCreatureComponent == null) return false;

        ChestTaskComponent chestTaskComponent = minionComponent.getChestTask();
        chestTaskComponent.setFinished(true);
        hexCreatureComponent.removeChestTask(chestTaskComponent);
        this.registerChestInMemory(sensorInfo, store, hexCreatureComponent);
        return true;
    }

    private void registerChestInMemory(InfoProvider sensorInfo, Store<EntityStore> store, HexCreatureComponent hexCreatureComponent) {
        if (sensorInfo != null) {
            IPositionProvider positionProvider = sensorInfo.getPositionProvider();
            if (positionProvider != null && !positionProvider.hasPosition()) {
                World world = store.getExternalData().getWorld();
                long chunkIndex = ChunkUtil.indexChunkFromBlock(positionProvider.getX(), positionProvider.getZ());
                Ref<ChunkStore> chunkRef = world.getChunkStore().getChunkReference(chunkIndex);
                if (chunkRef != null) {
                    Vector3d chestPosition = new Vector3d(
                            Math.floor(positionProvider.getX()),
                            Math.floor(positionProvider.getY()),
                            Math.floor(positionProvider.getZ())
                    );

                    Store<ChunkStore> chunkComponentStore = world.getChunkStore().getStore();
                    ChunkStore chunkStore = world.getChunkStore();
                    Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReferenceAtBlock((int)chestPosition.x, (int)chestPosition.y, (int)chestPosition.z);
                    if (sectionRef != null) {
                        Ref<ChunkStore> blockRef = BlockModule.getBlockEntity(chunkComponentStore, sectionRef, (int)chestPosition.x, (int)chestPosition.y, (int)chestPosition.z);
                        if (blockRef != null) {
                            ItemContainerBlock itemContainerBlock = chunkComponentStore.getComponent(blockRef, ItemContainerBlock.getComponentType());
                            if (itemContainerBlock != null) {
                                SimpleItemContainer chestContainer = itemContainerBlock.getItemContainer();
                                String[] differentItemsOnChest = InventoryUtils.getItemIdsFromContainer(chestContainer);

                                ChestMemoryComponent chestMemoryComponent = new ChestMemoryComponent(chestPosition, differentItemsOnChest);
                                hexCreatureComponent.addChestMemory(chestMemoryComponent);
                            }
                        }
                    }
                }
            }
        }
    }
}
