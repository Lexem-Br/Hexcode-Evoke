package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionTakeItemFromChestTask;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionTransferItemsToTargetNPC;
import com.lexem.hexcodeevoke.utils.InventoryUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class ActionTransferItemsToTargetNPC extends ActionBase {

    public ActionTransferItemsToTargetNPC(@Nonnull BuilderActionTransferItemsToTargetNPC builderActionBase) {
        super(builderActionBase);
    }

    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, executionSupport, sensorInfo, dt, store);

        HexCreatureMinionComponent minionComponent = store.getComponent(npcRef, HexCreatureMinionComponent.getComponentType());
        if (minionComponent == null) return false;

        UUID uuid = UUID.fromString(minionComponent.getOwnerUUID());
        Ref<EntityStore> hcRef = store.getExternalData().getRefFromUUID(uuid);
        if (hcRef == null) return false;

        CombinedItemContainer npcContainer = InventoryComponent.getCombined(
                store,
                npcRef,
                InventoryComponent.Hotbar.getComponentType()
        );

        CombinedItemContainer targetContainer = InventoryComponent.getCombined(
                store,
                hcRef,
                InventoryComponent.Storage.getComponentType(),
                InventoryComponent.Hotbar.getComponentType()
        );

        String itemToTransferId = minionComponent.getChestTask().getItemId();
        return InventoryUtils.transferAllItemsOfType(npcContainer, targetContainer, itemToTransferId, -1);
    }
}