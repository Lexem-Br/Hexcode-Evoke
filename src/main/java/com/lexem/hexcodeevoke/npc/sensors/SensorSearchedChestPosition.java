package com.lexem.hexcodeevoke.npc.sensors;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorSearchedChestPosition;
import com.lexem.hexcodeevoke.utils.ChestViewpointUtils;
import org.joml.Vector3d;

import javax.annotation.Nonnull;

public class SensorSearchedChestPosition extends SensorBase {
    protected final double range;
    protected final double minRange;
    protected final double heightOffset;
    protected final BuilderSensorSearchedChestPosition.ChestTarget chestTarget;
    private final PositionProvider positionProvider = new PositionProvider();
    private final Vector3d target = new Vector3d();

    public SensorSearchedChestPosition(@Nonnull BuilderSensorSearchedChestPosition builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.range = builder.getRange(support);
        this.minRange = builder.getMinRange(support);
        this.heightOffset = builder.getHeightOffset(support);
        this.chestTarget = builder.getTarget();
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull ExecutionSupport executionSupport, double dt, @Nonnull Store<EntityStore> store) {
        if (!super.matches(ref, executionSupport, dt, store)) {
            this.positionProvider.clear();
            return false;
        }

        HexCreatureComponent hexCreature = store.getComponent(ref, HexCreatureComponent.getComponentType());
        Vector3d chestPosition = hexCreature != null ? this.chestTarget.getPosition(hexCreature) : null;
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (chestPosition == null || transform == null) {
            this.positionProvider.clear();
            return false;
        }

        MotionController motionController = executionSupport.getMotionContextSupport().getActiveMotionController();
        ChestViewpointUtils.findViewpoint(chestPosition, this.heightOffset, store.getExternalData().getWorld(), motionController, store, this.target);

        double distanceSquared = transform.getPosition().distanceSquared(this.target);
        if (distanceSquared > this.range * this.range || distanceSquared < this.minRange * this.minRange) {
            this.positionProvider.clear();
            return false;
        }

        this.positionProvider.setTarget(this.target);
        return true;
    }

    @Override
    public InfoProvider getSensorInfo() {
        return this.positionProvider;
    }
}
