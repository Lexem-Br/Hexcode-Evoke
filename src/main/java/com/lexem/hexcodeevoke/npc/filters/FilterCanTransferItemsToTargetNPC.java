package com.lexem.hexcodeevoke.npc.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.lexem.hexcodeevoke.npc.filters.builders.BuilderFilterCanTransferItemsToTargetNPC;
import com.lexem.hexcodeevoke.utils.InventoryUtils;

import javax.annotation.Nonnull;

public class FilterCanTransferItemsToTargetNPC extends EntityFilterBase {
    protected boolean reverse;

    public FilterCanTransferItemsToTargetNPC(@Nonnull BuilderFilterCanTransferItemsToTargetNPC builder) {
        this.reverse = builder.getReverse();
    }

    @Override
    public boolean matchesEntity(
            @Nonnull Ref<EntityStore> npcRef,
            @Nonnull Ref<EntityStore> targetRef,
            @Nonnull ExecutionSupport executionSupport,
            @Nonnull Store<EntityStore> store
    ) {
        CombinedItemContainer npcContainer = InventoryComponent.getCombined(
                store,
                npcRef,
                InventoryComponent.Hotbar.getComponentType()
        );

        CombinedItemContainer targetContainer = InventoryComponent.getCombined(
                store,
                targetRef,
                InventoryComponent.Storage.getComponentType(),
                InventoryComponent.Hotbar.getComponentType()
        );

        boolean canReturn = InventoryUtils.canReturnAnyItemToOwner(npcContainer, targetContainer);
        return reverse != canReturn;
    }

    @Override
    public int cost() {
        return 300;
    }
}