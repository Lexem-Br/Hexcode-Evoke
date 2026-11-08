package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.IPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionRefillWateringCan;
import com.lexem.hexcodeevoke.utils.HeldItemUtils;
import com.lexem.hexcodeevoke.utils.WateringCanUtils;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionRefillWateringCan extends ActionBase {

    public ActionRefillWateringCan(@Nonnull BuilderActionRefillWateringCan builder) {
        super(builder);
    }

    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, executionSupport, sensorInfo, dt, store);

        if (sensorInfo == null || !sensorInfo.hasPosition()) return false;

        IPositionProvider positionProvider = sensorInfo.getPositionProvider();
        if (positionProvider == null || !positionProvider.hasPosition()) return false;

        Vector3i waterPosition = new Vector3i(
                (int) Math.floor(positionProvider.getX()),
                (int) Math.floor(positionProvider.getY()),
                (int) Math.floor(positionProvider.getZ())
        );
        if (!WateringCanUtils.isWater(waterPosition, store.getExternalData().getWorld())) return false;

        HeldItemUtils.HeldItem can = WateringCanUtils.findRefillableCan(npcRef, store);
        return can != null && WateringCanUtils.refill(can);
    }
}
