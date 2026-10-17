package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
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
import com.lexem.hexcodeevoke.utils.ChestMemoryUtils;
import com.lexem.hexcodeevoke.utils.InventoryUtils;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ActionAnalyzeChest extends ActionBase {
   public static HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

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

      ChestMemoryUtils.ChestLookup chestLookup = ChestMemoryUtils.findChest(world, chestPosition);
      if (!chestLookup.missing() && chestLookup.container() == null) return false;

      SimpleItemContainer chestContainer = chestLookup.container();

      HexCreatureMinionComponent minionComponent = store.getComponent(npcRef, HexCreatureMinionComponent.getComponentType());
      if (minionComponent == null) return false;

      UUID uuid = UUID.fromString(minionComponent.getOwnerUUID());
      Ref<EntityStore> hcRef = store.getExternalData().getRefFromUUID(uuid);
      if (hcRef == null) return false;

      HexCreatureComponent hexCreatureComponent = store.getComponent(hcRef, HexCreatureComponent.getComponentType());
      if (hexCreatureComponent == null) return false;

      ChestTaskComponent chestTask = minionComponent.getChestTask();
      if (chestTask == null) return false;

      if (chestLookup.missing()) {
         hexCreatureComponent.forgetChest(chestPosition);
      }

      String itemId = chestTask.getItemId();
      int itemQuantity = chestTask.getItemQuantity();

      boolean itemExistsOnChest = chestContainer != null && InventoryUtils.containsItemId(chestContainer, itemId);
      boolean canStoreAny = chestContainer != null && InventoryUtils.canAddAnyQuantityOfItem(chestContainer, itemId);

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
         List<ChestTaskComponent> pendingTasks = getPendingTasks(hexCreatureComponent, chestTask, store);

         boolean hasOtherTaskForItem = false;
         List<Vector3d> reservedChests = new ArrayList<>();
         for (ChestTaskComponent task : pendingTasks) {
            if (itemId.equals(task.getItemId())) {
               hasOtherTaskForItem = true;
            } else if (task.getTaskType() == ChestTaskComponent.TaskType.Store) {
               reservedChests.add(task.getChestPosition());
            }
         }

         if (!hasOtherTaskForItem) {
            TransformComponent transformComponent = store.getComponent(npcRef, TransformComponent.getComponentType());
            Vector3d npcPosition = transformComponent != null ? transformComponent.getPosition() : chestPosition;

            ChestMemoryComponent targetChestMemory = hexCreatureComponent.findEmptyChestMemory(reservedChests);
            if (targetChestMemory == null) {
               targetChestMemory = hexCreatureComponent.findNearestChestMemory(npcPosition, reservedChests);
            }
            if (targetChestMemory == null) {
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
      ChestMemoryUtils.registerChestInMemory(sensorInfo, store, hexCreatureComponent);
      chestTask.setFinished(true);
      return true;
   }

   private static List<ChestTaskComponent> getPendingTasks(
           @Nonnull HexCreatureComponent hexCreatureComponent,
           @Nonnull ChestTaskComponent currentTask,
           @Nonnull Store<EntityStore> store
   ) {
      List<ChestTaskComponent> pendingTasks = new ArrayList<>();

      ChestTaskComponent[] tasks = hexCreatureComponent.getListChestTask();
      if (tasks != null) {
         for (ChestTaskComponent task : tasks) {
            if (task != null && task != currentTask && !task.isFinished()) pendingTasks.add(task);
         }
      }

      String[] minionUUIDs = hexCreatureComponent.getMinionUUIDs();
      if (minionUUIDs != null) {
         for (String minionUUID : minionUUIDs) {
            Ref<EntityStore> minionRef = store.getExternalData().getRefFromUUID(UUID.fromString(minionUUID));
            if (minionRef == null) continue;

            HexCreatureMinionComponent minion = store.getComponent(minionRef, HexCreatureMinionComponent.getComponentType());
            if (minion == null) continue;

            ChestTaskComponent task = minion.getChestTask();
            if (task != null && task != currentTask && !task.isFinished() && !pendingTasks.contains(task)) pendingTasks.add(task);
         }
      }

      return pendingTasks;
   }
}