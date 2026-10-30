package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.Dirty;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionTakeTrayEssence;
import com.lexem.hexcodeevoke.utils.EssenceTrayUtils;
import com.lexem.hexcodeevoke.utils.LifeEssenceUtils;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionTakeTrayEssence extends ActionBase {

    public ActionTakeTrayEssence(@Nonnull BuilderActionTakeTrayEssence builder) {
        super(builder);
    }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, executionSupport, sensorInfo, dt, store);

        HexCreatureComponent hexCreature = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        Vector3i tray = hexCreature != null ? hexCreature.getRefuelState().getTray() : null;
        if (tray == null || hexCreature.isLifeEssenceDepleted()) return false;

        float capacity = LifeEssenceUtils.getCapacity(LifeEssenceUtils.getHexItem(hexCreature));
        int missing = LifeEssenceUtils.getMissingUnits(hexCreature.getLifeEssence(), capacity);
        int taken = EssenceTrayUtils.takeLifeEssence(store.getExternalData().getWorld(), tray, missing);
        if (taken <= 0) return true;

        hexCreature.setLifeEssence(hexCreature.getLifeEssence() + taken);
        Dirty dirty = store.getComponent(npcRef, Dirty.getComponentType());
        if (dirty != null) dirty.markDirty();
        LifeEssenceUtils.syncToEvoker(store, hexCreature);
        return true;
    }
}
