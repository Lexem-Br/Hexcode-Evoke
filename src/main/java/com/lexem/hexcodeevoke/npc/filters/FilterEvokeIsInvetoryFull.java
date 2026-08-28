package com.lexem.hexcodeevoke.npc.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.util.InventoryHelper;
import com.lexem.hexcodeevoke.npc.filters.builders.BuilderFilterIsInventoryFull;

import javax.annotation.Nonnull;
import java.util.List;

public class FilterEvokeIsInvetoryFull extends EntityFilterBase {
    protected final List<String> inventoryTypes;

   public FilterEvokeIsInvetoryFull(@Nonnull BuilderFilterIsInventoryFull builder, @Nonnull BuilderSupport support) {
       String[] inventoryArray = builder.getInventoryTypes(support);
       this.inventoryTypes = inventoryArray != null ? List.of(inventoryArray) : null;
   }

   @Override
   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull ExecutionSupport executionSupport, @Nonnull Store<EntityStore> store) {
       int freeSlots = 0;
       for (String type : inventoryTypes) {
          int freeSlotsType = switch (type) {
              case "Armor" -> getFreeSlots(store.getComponent(ref, InventoryComponent.Armor.getComponentType()));
              case "Hotbar" -> getFreeSlotsHotbar(store.getComponent(ref, InventoryComponent.Hotbar.getComponentType()));
              case "Storage" -> getFreeSlots(store.getComponent(ref, InventoryComponent.Storage.getComponentType()));
              case "Utility" -> getFreeSlots(store.getComponent(ref, InventoryComponent.Utility.getComponentType()));
              case "Tool" -> getFreeSlots(store.getComponent(ref, InventoryComponent.Tool.getComponentType()));
              case "Backpack" -> getFreeSlots(store.getComponent(ref, InventoryComponent.Backpack.getComponentType()));
              default -> 0;
          };

          freeSlots += freeSlotsType;
       }

       return freeSlots == 0;
   }

   private int getFreeSlotsHotbar(InventoryComponent inventoryComponent) {
      if (inventoryComponent != null) {
         ItemContainer container = inventoryComponent.getInventory();
         ItemStack itemStack = container.getItemStack((short) 0);
         if (ItemStack.isEmpty(itemStack)) {
             return InventoryHelper.countFreeSlots(container) - 1;
         } else {
             return InventoryHelper.countFreeSlots(container);
         }
      }
      return 0;
   }

    private int getFreeSlots(InventoryComponent inventoryComponent) {
        if (inventoryComponent != null) {
            ItemContainer container = inventoryComponent.getInventory();
            return InventoryHelper.countFreeSlots(container);
        }
        return 0;
    }

   @Override
   public int cost() {
      return 300;
   }
}
