package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.IPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.components.ChestMemoryComponent;
import com.lexem.hexcodeevoke.components.ChestTaskComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionAnalyzeChest;
import com.lexem.hexcodeevoke.utils.InventoryUtils;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ActionAnalyzeChest extends ActionBase {

   public ActionAnalyzeChest(@Nonnull BuilderActionAnalyzeChest builder, @Nonnull BuilderSupport support) {
      super(builder);
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(npcRef, executionSupport, sensorInfo, dt, store);
      if (sensorInfo == null) return false;

      IPositionProvider positionProvider = sensorInfo.getPositionProvider();
      if (positionProvider == null || !positionProvider.hasPosition()) return false;

      World world = store.getExternalData().getWorld();
      Vector3d chestPosition = new Vector3d(
              Math.floor(positionProvider.getX()),
              Math.floor(positionProvider.getY()),
              Math.floor(positionProvider.getZ())
      );

      long chunkIndex = ChunkUtil.indexChunkFromBlock(positionProvider.getX(), positionProvider.getZ());
      Ref<ChunkStore> chunkRef = world.getChunkStore().getChunkReference(chunkIndex);
      if (chunkRef == null) return false;

      Store<ChunkStore> chunkComponentStore = world.getChunkStore().getStore();
      ChunkStore chunkStore = world.getChunkStore();
      Ref<ChunkStore> sectionRef = chunkStore.getChunkSectionReferenceAtBlock((int)chestPosition.x, (int)chestPosition.y, (int)chestPosition.z);
      if (sectionRef == null) return false;

      Ref<ChunkStore> blockRef = BlockModule.getBlockEntity(chunkComponentStore, sectionRef, (int)chestPosition.x, (int)chestPosition.y, (int)chestPosition.z);
      if (blockRef == null) return false;

      ItemContainerBlock itemContainerBlock = chunkComponentStore.getComponent(blockRef, ItemContainerBlock.getComponentType());
      if (itemContainerBlock == null) return false;

      SimpleItemContainer chestContainer = itemContainerBlock.getItemContainer();
      String[] differentItemsOnChest = InventoryUtils.getItemIdsFromContainer(chestContainer);

      HexCreatureMinionComponent minionComponent = store.getComponent(npcRef, HexCreatureMinionComponent.getComponentType());
      if (minionComponent == null) return false;

      UUID uuid = UUID.fromString(minionComponent.getOwnerUUID());
      Ref<EntityStore> hcRef = store.getExternalData().getRefFromUUID(uuid);
      if (hcRef == null) return false;

      HexCreatureComponent hexCreatureComponent = store.getComponent(hcRef, HexCreatureComponent.getComponentType());
      if (hexCreatureComponent == null) return false;

      ChestMemoryComponent chestMemoryComponent = new ChestMemoryComponent(chestPosition, differentItemsOnChest);
      hexCreatureComponent.addChestMemory(chestMemoryComponent);

      ChestTaskComponent chestTask = minionComponent.getChestTask();
      String itemId = chestTask.getItemId();
      int itemQuantity = chestTask.getItemQuantity();

      boolean itemExistsOnChest = InventoryUtils.containsItemId(chestContainer, itemId);
      boolean canStoreAny = InventoryUtils.canAddAnyQuantityOfItem(chestContainer, itemId);

      if (itemExistsOnChest && canStoreAny) {
         hexCreatureComponent.removeChestTasksByItemId(itemId);

         ChestTaskComponent chestTaskComponent = new ChestTaskComponent(
                 ChestTaskComponent.TaskType.Store,
                 chestPosition,
                 itemId,
                 itemQuantity,
                 false

         );
         hexCreatureComponent.addChestTask(chestTaskComponent);
      } else {
         boolean hasOtherTaskForItem = false;
         ChestTaskComponent[] tasks = hexCreatureComponent.getListChestTask();

         for (ChestTaskComponent task : tasks) {
            if (task != null && task != chestTask && itemId.equals(task.getItemId())) {
               hasOtherTaskForItem = true;
               break;
            }
         }

         if (!hasOtherTaskForItem) {
            TransformComponent transformComponent = store.getComponent(npcRef, TransformComponent.getComponentType());
            Vector3d npcPosition = transformComponent != null ? transformComponent.getPosition() : chestPosition;

            ChestMemoryComponent emptyChestMemory = hexCreatureComponent.findEmptyChestMemory();
            ChestMemoryComponent targetChestMemory;

            if (emptyChestMemory != null) {
               targetChestMemory = emptyChestMemory;
            } else {
               targetChestMemory = hexCreatureComponent.findNearestChestMemory(npcPosition);
            }

            if (targetChestMemory != null) {
               Vector3d targetPosition = targetChestMemory.getChestPosition();

               ChestTaskComponent newStoreTask = new ChestTaskComponent(
                       ChestTaskComponent.TaskType.Store,
                       targetPosition,
                       itemId,
                       itemQuantity,
                       false
               );
               hexCreatureComponent.addChestTask(newStoreTask);
            }
         }
         hexCreatureComponent.removeChestTask(chestTask);
      }

      chestTask.setFinished(true);
      return true;
   }

}