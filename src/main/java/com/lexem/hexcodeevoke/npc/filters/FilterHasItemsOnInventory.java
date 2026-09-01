package com.lexem.hexcodeevoke.npc.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;

import javax.annotation.Nonnull;

public class FilterHasItemsOnInventory extends EntityFilterBase {

   public FilterHasItemsOnInventory() {}

    @Override
    public boolean matchesEntity(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Ref<EntityStore> targetRef,
            @Nonnull ExecutionSupport executionSupport,
            @Nonnull Store<EntityStore> store
    ) {
        InventoryComponent inventoryComponent = store.getComponent(ref, InventoryComponent.Storage.getComponentType());
        if (inventoryComponent == null)  return false;

        ItemContainer container = inventoryComponent.getInventory();
        if (container == null)  return false;

        return !container.isEmpty();
    }

   @Override
   public int cost() {
      return 300;
   }
}
