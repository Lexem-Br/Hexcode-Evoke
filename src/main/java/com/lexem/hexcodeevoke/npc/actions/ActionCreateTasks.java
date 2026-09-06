package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.components.ChestMemoryComponent;
import com.lexem.hexcodeevoke.components.ChestTaskComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionCreateTasks;
import com.lexem.hexcodeevoke.utils.FinderUtils;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ActionCreateTasks extends ActionBase {
   private final double horizontalRange;
   private final double verticalRange;
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   public ActionCreateTasks(@Nonnull BuilderActionCreateTasks builder,  @Nonnull BuilderSupport support) {
      super(builder);
      this.horizontalRange = builder.getHorizontalRange(support);
      this.verticalRange = builder.getVerticalRange(support);
   }

   @Override
   public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      super.execute(npcRef, executionSupport, sensorInfo, dt, store);

      World world = store.getExternalData().getWorld();

      HexCreatureComponent hexCreatureComponent = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
      if (hexCreatureComponent == null) return false;

      if (!hexCreatureComponent.isListChestTaskEmpty()) return false;
      if (hasAnyTaskToFinish(hexCreatureComponent, store)) return false;

      TransformComponent transformComponent = store.getComponent(npcRef, TransformComponent.getComponentType());
      if (transformComponent == null) return false;

      List<ItemData> listNPCChestItems = getChestItemsId(store, npcRef);
      if (listNPCChestItems.isEmpty()) return false;

      List<Vector3d> listChestsPosition = FinderUtils.findAllChestsInRange(
              transformComponent.getPosition(),
              (int) Math.ceil(horizontalRange),
              (int) Math.ceil(verticalRange),
              world
      );
      if (listChestsPosition.isEmpty()) return false;

      for (Vector3d chestPosition : listChestsPosition) {
         for (ItemData npcChestItem : listNPCChestItems) {
            ChestTaskComponent chestTaskComponent = getChestTaskComponent(chestPosition, npcChestItem, hexCreatureComponent);
            hexCreatureComponent.addChestTask(chestTaskComponent);
         }
      }

      LOGGER.atInfo().log("ChestTaskComponent: %s", Arrays.toString(hexCreatureComponent.getListChestTask()));
      return true;
   }

   private static ChestTaskComponent getChestTaskComponent(
           Vector3d chestPosition,
           ItemData npcChestItem,
           HexCreatureComponent hexCreatureComponent
   ) {
      ChestTaskComponent chestTaskComponent;
      ChestMemoryComponent chestMemoryComponent = hexCreatureComponent.findChestMemoryByItemId(npcChestItem.itemId);

      if (chestMemoryComponent != null) {
         chestTaskComponent = new ChestTaskComponent(
                 ChestTaskComponent.TaskType.Store,
                 chestMemoryComponent.getChestPosition(),
                 npcChestItem.itemId,
                 npcChestItem.itemQuantity,
                 false
         );
      } else {
         chestTaskComponent = new ChestTaskComponent(
                 ChestTaskComponent.TaskType.Search,
                 chestPosition,
                 npcChestItem.itemId,
                 npcChestItem.itemQuantity,
                 false
         );
      }

      return chestTaskComponent;
   }

   private List<ItemData> getChestItemsId(Store<EntityStore> store, Ref<EntityStore> npcRef) {
      List<ItemData> listItemData = new ArrayList<>();

      ItemContainer itemContainer = Objects.requireNonNull(store.getComponent(npcRef, InventoryComponent.Storage.getComponentType())).getInventory();

      for (short slot = 0; slot < itemContainer.getCapacity(); slot++) {
         ItemStack itemStack = itemContainer.getItemStack(slot);
         if (!ItemStack.isEmpty(itemStack)) {
            String itemId = itemStack.getItem().getId();
            int itemQuantity = itemStack.getQuantity();

            boolean found = false;
            for (ItemData existingItem : listItemData) {
               if (existingItem.itemId.equals(itemId)) {
                  int newQuantity = existingItem.itemQuantity + itemQuantity;
                  listItemData.remove(existingItem);
                  listItemData.add(new ItemData(itemId, newQuantity));
                  found = true;
                  break;
               }
            }

            if (!found) {
               listItemData.add(new ItemData(itemId, itemQuantity));
            }
         }
      }

      return listItemData;
   }

   private boolean hasAnyTaskToFinish(HexCreatureComponent hexCreatureComponent, Store<EntityStore> store) {
      String[] minionUUIDs = hexCreatureComponent.getMinionUUIDs();
      if (minionUUIDs == null) return false;

      for (String uuidString : minionUUIDs) {
         UUID uuid = UUID.fromString(uuidString);
         Ref<EntityStore> minionRef = store.getExternalData().getRefFromUUID(uuid);
         if (minionRef == null) continue;

         HexCreatureMinionComponent minionComponent = store.getComponent(minionRef, HexCreatureMinionComponent.getComponentType());
         if (minionComponent == null) continue;

         ChestTaskComponent chestTask = minionComponent.getChestTask();
         if (chestTask != null && !chestTask.isFinished()) return true;
      }

      return false;
   }

   private record ItemData(@Nonnull String itemId, int itemQuantity) {}

}