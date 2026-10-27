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
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorRefuelPosition;
import com.lexem.hexcodeevoke.utils.EssenceTrayUtils;
import com.lexem.hexcodeevoke.utils.RefuelState;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SensorRefuelPosition extends SensorBase {
    private final RefuelState.Phase target;
    private final double range;
    private final double minRange;
    private final double heightOffset;
    private final double timeout;
    private final PositionProvider positionProvider = new PositionProvider();

    public SensorRefuelPosition(@Nonnull BuilderSensorRefuelPosition builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.target = builder.getTarget();
        this.range = builder.getRange(support);
        this.minRange = builder.getMinRange(support);
        this.heightOffset = builder.getHeightOffset(support);
        this.timeout = builder.getTimeout(support);
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, double dt, @Nonnull Store<EntityStore> store) {
        this.positionProvider.clear();
        if (!super.matches(npcRef, executionSupport, dt, store)) return false;

        HexCreatureComponent hexCreature = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        TransformComponent transform = store.getComponent(npcRef, TransformComponent.getComponentType());
        if (hexCreature == null || transform == null) return false;

        RefuelState refuel = hexCreature.getRefuelState();
        if (this.timeout > 0 && refuel.getPhaseSeconds(this.target) > this.timeout) return false;

        Vector3d position = this.getTargetPosition(refuel, store);
        if (position == null) return false;

        position.add(0, this.heightOffset, 0);
        double distance = transform.getPosition().distanceSquared(position);
        if (distance > this.range * this.range || distance < this.minRange * this.minRange) return false;

        this.positionProvider.setTarget(position);
        return true;
    }

    @Nullable
    private Vector3d getTargetPosition(@Nonnull RefuelState refuel, @Nonnull Store<EntityStore> store) {
        if (this.target == RefuelState.Phase.Return) {
            Vector3d returnPosition = refuel.getReturnPosition();
            return returnPosition != null ? new Vector3d(returnPosition) : null;
        }

        Vector3i tray = refuel.getTray();
        if (tray == null || EssenceTrayUtils.countLifeEssence(store.getExternalData().getWorld(), tray) <= 0) return null;
        return EssenceTrayUtils.getCenter(tray);
    }

    @Override
    public InfoProvider getSensorInfo() {
        return this.positionProvider;
    }
}
