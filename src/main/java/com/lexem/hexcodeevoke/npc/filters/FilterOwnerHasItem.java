package com.lexem.hexcodeevoke.npc.filters;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.npc.filters.builders.BuilderOwnerHasItem;
import com.lexem.hexcodeevoke.utils.InventoryUtils;

import javax.annotation.Nonnull;
import java.util.UUID;

public class FilterOwnerHasItem extends EntityFilterBase {
    protected boolean reverse;

    public FilterOwnerHasItem(@Nonnull BuilderOwnerHasItem builder) {
        this.reverse = builder.getReverse();
    }

    @Override
    public boolean matchesEntity(@Nonnull Ref<EntityStore> npcRef, @Nonnull Ref<EntityStore> targetRef, @Nonnull ExecutionSupport executionSupport, @Nonnull Store<EntityStore> store) {
        HexCreatureMinionComponent minionComponent = store.getComponent(npcRef, HexCreatureMinionComponent.getComponentType());
        if (minionComponent == null) return false;

        UUID uuid = UUID.fromString(minionComponent.getOwnerUUID());
        Ref<EntityStore> hcRef = store.getExternalData().getRefFromUUID(uuid);
        if (hcRef == null) return false;

        HexCreatureComponent hexCreatureComponent = store.getComponent(hcRef, HexCreatureComponent.getComponentType());
        if (hexCreatureComponent == null) return false;

        String itemId = minionComponent.getChestTask().getItemId();
        if (itemId == null || itemId.isEmpty()) return false;

        CombinedItemContainer combinedContainer = InventoryComponent.getCombined(
                store,
                hcRef,
                InventoryComponent.Storage.getComponentType(),
                InventoryComponent.Hotbar.getComponentType()
        );

        boolean hasItem = InventoryUtils.containsItemId(combinedContainer, itemId);
        return reverse != hasItem;
    }

   @Override
   public int cost() {
      return 300;
   }
}
