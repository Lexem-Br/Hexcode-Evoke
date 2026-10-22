package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionMemorizeChest;
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorSearchedChestPosition;
import com.lexem.hexcodeevoke.utils.ChestMemoryUtils;
import com.lexem.hexcodeevoke.utils.InventoryUtils;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionMemorizeChest extends ActionBase {
    protected final BuilderSensorSearchedChestPosition.ChestTarget chestTarget;

    public ActionMemorizeChest(@Nonnull BuilderActionMemorizeChest builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.chestTarget = builder.getTarget();
    }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, executionSupport, sensorInfo, dt, store);

        HexCreatureComponent hexCreature = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        Vector3d chestPosition = hexCreature != null ? this.chestTarget.getPosition(hexCreature) : null;
        if (chestPosition == null) return false;

        Vector3d chestBlock = new Vector3d(Math.floor(chestPosition.x), Math.floor(chestPosition.y), Math.floor(chestPosition.z));
        ChestMemoryUtils.ChestLookup lookup = ChestMemoryUtils.findChest(store.getExternalData().getWorld(), chestBlock);
        if (lookup.missing()) {
            hexCreature.forgetChest(chestBlock);
        } else if (lookup.container() != null) {
            hexCreature.updateChestMemory(chestBlock, InventoryUtils.getItemIdsFromContainer(lookup.container()));
        }

        hexCreature.markChestVisited(chestBlock);
        if (this.chestTarget == BuilderSensorSearchedChestPosition.ChestTarget.Explore) {
            hexCreature.setExploreChestPosition(null);
        }
        return true;
    }
}
