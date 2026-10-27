package com.lexem.hexcodeevoke.npc.sensors;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorNeedsEssenceTray;
import com.lexem.hexcodeevoke.utils.EssenceTrayUtils;
import com.lexem.hexcodeevoke.utils.LifeEssenceUtils;
import com.lexem.hexcodeevoke.utils.RefuelState;
import org.joml.Vector3i;

import javax.annotation.Nonnull;

public class SensorNeedsEssenceTray extends SensorBase {
    private static final double CHECK_INTERVAL = 1.0;

    private final double range;
    private final double threshold;
    private final PositionProvider positionProvider = new PositionProvider();
    private double checkTimer;

    public SensorNeedsEssenceTray(@Nonnull BuilderSensorNeedsEssenceTray builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.range = builder.getRange(support);
        this.threshold = builder.getThreshold(support);
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, double dt, @Nonnull Store<EntityStore> store) {
        this.positionProvider.clear();
        if (!super.matches(npcRef, executionSupport, dt, store)) return false;

        this.checkTimer -= dt;
        if (this.checkTimer > 0) return false;
        this.checkTimer = CHECK_INTERVAL;

        HexCreatureComponent hexCreature = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        TransformComponent transform = store.getComponent(npcRef, TransformComponent.getComponentType());
        if (hexCreature == null || transform == null || !this.needsLifeEssence(hexCreature)) return false;
        if (hexCreature.getRefuelState().isOnCooldown() || RefuelState.isRefueling(executionSupport.getStateSupport())) return false;

        Vector3i tray = EssenceTrayUtils.findNearestFilledTray(store.getExternalData().getWorld(), transform.getPosition(), this.range);
        if (tray == null) return false;

        this.positionProvider.setTarget(EssenceTrayUtils.getCenter(tray));
        return true;
    }

    private boolean needsLifeEssence(@Nonnull HexCreatureComponent hexCreature) {
        if (hexCreature.getEvokerUUID() == null || hexCreature.isLifeEssenceDepleted() || !hexCreature.hasLifeEssence()) return false;

        AllowedHexItemsAsset.HexItem hexItem = LifeEssenceUtils.getHexItem(hexCreature);
        if (!LifeEssenceUtils.consumesLifeEssence(hexItem)) return false;

        float capacity = LifeEssenceUtils.getCapacity(hexItem);
        float stored = hexCreature.getLifeEssence();
        return stored <= capacity * this.threshold && LifeEssenceUtils.getMissingUnits(stored, capacity) > 0;
    }

    @Override
    public InfoProvider getSensorInfo() {
        return this.positionProvider;
    }
}
