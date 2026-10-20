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
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorIsStuck;
import org.joml.Vector3d;

import javax.annotation.Nonnull;

public class SensorIsStuck extends SensorBase {
    private final double delay;
    private final double minProgress;
    private final Vector3d anchor = new Vector3d();
    private boolean hasAnchor;
    private double stuckTime;
    private final double maxStuckTime;
    private boolean stuck;

    public SensorIsStuck(@Nonnull BuilderSensorIsStuck builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.delay = builder.getDelay(support);
        this.minProgress = builder.getMinProgress(support);
        this.maxStuckTime = builder.getMaxStuckTime(support);
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull ExecutionSupport executionSupport, double dt, @Nonnull Store<EntityStore> store) {
        if (!super.matches(ref, executionSupport, dt, store)) {
            this.reset();
            return false;
        }

        MotionController motionController = executionSupport.getMotionContextSupport().getActiveMotionController();
        TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
        if (motionController == null || transformComponent == null) {
            this.reset();
            return false;
        }

        Vector3d position = transformComponent.getPosition();

        if (this.stuck) {
            this.stuckTime += dt;
            boolean insideBlocks = motionController.isObstructed() || !motionController.isValidPosition(position, store);
            if (insideBlocks && this.stuckTime < this.maxStuckTime) return true;

            this.reset();
            return false;
        }

        if (!motionController.isObstructed()) {
            this.reset();
            return false;
        }

        if (!this.hasAnchor) {
            this.anchor.set(position);
            this.hasAnchor = true;
            this.stuckTime = 0;
        }

        double dx = position.x - this.anchor.x;
        double dz = position.z - this.anchor.z;
        if (dx * dx + dz * dz > this.minProgress * this.minProgress) {
            this.anchor.set(position);
            this.stuckTime = 0;
            return false;
        }

        this.stuckTime += dt;
        if (this.stuckTime < this.delay) return false;

        this.stuck = true;
        this.stuckTime = 0;
        return true;
    }

    private void reset() {
        this.hasAnchor = false;
        this.stuckTime = 0;
        this.stuck = false;
    }

    @Override
    public InfoProvider getSensorInfo() {
        return null;
    }
}
