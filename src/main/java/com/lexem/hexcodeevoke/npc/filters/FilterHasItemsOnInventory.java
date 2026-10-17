package com.lexem.hexcodeevoke.npc.filters;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.lexem.hexcodeevoke.npc.filters.builders.BuilderFilterHasItemsOnInventory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class FilterHasItemsOnInventory extends EntityFilterBase {

    protected final ComponentType<EntityStore, ? extends InventoryComponent>[] inventoryTypes;

    @SuppressWarnings("unchecked")
    public FilterHasItemsOnInventory(@Nonnull BuilderFilterHasItemsOnInventory builder, @Nonnull BuilderSupport support) {
        List<ComponentType<EntityStore, ? extends InventoryComponent>> types = new ArrayList<>();
        String[] inventoryArray = builder.getInventoryTypes(support);
        if (inventoryArray != null) {
            for (String type : inventoryArray) {
                ComponentType<EntityStore, ? extends InventoryComponent> componentType = toComponentType(type);
                if (componentType != null && !types.contains(componentType)) types.add(componentType);
            }
        }
        this.inventoryTypes = types.toArray(new ComponentType[0]);
    }

    @Override
    public boolean matchesEntity(
            @Nonnull Ref<EntityStore> npcRef,
            @Nonnull Ref<EntityStore> targetRef,
            @Nonnull ExecutionSupport executionSupport,
            @Nonnull Store<EntityStore> store
    ) {
        if (inventoryTypes.length == 0) return false;

        CombinedItemContainer npcContainer = InventoryComponent.getCombined(store, npcRef, inventoryTypes);
        return !npcContainer.isEmpty();
    }

    private static ComponentType<EntityStore, ? extends InventoryComponent> toComponentType(String type) {
        return switch (type) {
            case "Armor" -> InventoryComponent.Armor.getComponentType();
            case "Hotbar" -> InventoryComponent.Hotbar.getComponentType();
            case "Storage" -> InventoryComponent.Storage.getComponentType();
            case "Utility" -> InventoryComponent.Utility.getComponentType();
            case "Tool" -> InventoryComponent.Tool.getComponentType();
            case "Backpack" -> InventoryComponent.Backpack.getComponentType();
            default -> null;
        };
    }

   @Override
   public int cost() {
      return 300;
   }
}
