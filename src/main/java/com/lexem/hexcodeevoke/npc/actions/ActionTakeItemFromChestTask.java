package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
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
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionTakeItemFromChestTask;
import com.lexem.hexcodeevoke.utils.InventoryUtils;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class ActionTakeItemFromChestTask extends ActionBase {

    public ActionTakeItemFromChestTask(@Nonnull BuilderActionTakeItemFromChestTask builderActionBase) {
        super(builderActionBase);
    }

    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, executionSupport, sensorInfo, dt, store);

        HexCreatureMinionComponent minionComponent = store.getComponent(npcRef, HexCreatureMinionComponent.getComponentType());
        if (minionComponent == null) return false;

        UUID uuid = UUID.fromString(minionComponent.getOwnerUUID());
        Ref<EntityStore> hcRef = store.getExternalData().getRefFromUUID(uuid);
        if (hcRef == null) return false;

        HexCreatureComponent hexCreatureComponent = store.getComponent(hcRef, HexCreatureComponent.getComponentType());
        if (hexCreatureComponent == null) return false;

        CombinedItemContainer hcCombinedContainer = InventoryComponent.getCombined(
                store,
                hcRef,
                InventoryComponent.Storage.getComponentType(),
                InventoryComponent.Hotbar.getComponentType()
        );

        CombinedItemContainer minionCombinedContainer = InventoryComponent.getCombined(
                store,
                npcRef,
                InventoryComponent.Storage.getComponentType(),
                InventoryComponent.Hotbar.getComponentType()
        );

        String itemToTransferId = minionComponent.getChestTask().getItemId();
        return InventoryUtils.transferAllItemsOfType(hcCombinedContainer, minionCombinedContainer, itemToTransferId);
    }
}