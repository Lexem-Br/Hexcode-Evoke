package com.lexem.hexcodeevoke.npc.sensors;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.components.ChestTaskComponent;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorChestTaskStuck;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class SensorChestTaskStuck extends SensorBase {
    private static final long RESET_GAP_NANOS = 1_000_000_000L;

    private final double stuckTime;
    private final double maxTime;
    private final double minProgress;
    private final BuilderSensorChestTaskStuck.Target target;

    @Nullable
    private ChestTaskComponent trackedTask;
    private double bestDistance;
    private double timeWithoutProgress;
    private double totalTime;
    private long lastMatchNanos;

    public SensorChestTaskStuck(@Nonnull BuilderSensorChestTaskStuck builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.stuckTime = builder.getStuckTime(support);
        this.maxTime = builder.getMaxTime(support);
        this.minProgress = builder.getMinProgress(support);
        this.target = builder.getTarget();
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull ExecutionSupport executionSupport, double dt, @Nonnull Store<EntityStore> store) {
        if (!super.matches(ref, executionSupport, dt, store)) return false;

        HexCreatureMinionComponent minionComponent = store.getComponent(ref, HexCreatureMinionComponent.getComponentType());
        TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
        if (minionComponent == null || transformComponent == null) return false;

        ChestTaskComponent chestTask = minionComponent.getChestTask();
        Vector3d targetPosition;
        if (this.target == BuilderSensorChestTaskStuck.Target.Owner) {
            targetPosition = getOwnerPosition(minionComponent, store);
        } else {
            if (chestTask == null || chestTask.isFinished()) {
                this.trackedTask = null;
                return false;
            }
            Vector3d chestPosition = chestTask.getChestPosition();
            targetPosition = new Vector3d(Math.floor(chestPosition.x) + 0.5, Math.floor(chestPosition.y), Math.floor(chestPosition.z) + 0.5);
        }

        double distance = targetPosition != null ? transformComponent.getPosition().distance(targetPosition) : Double.MAX_VALUE;

        long now = System.nanoTime();
        if (chestTask != this.trackedTask || now - this.lastMatchNanos > RESET_GAP_NANOS) {
            this.trackedTask = chestTask;
            this.bestDistance = distance;
            this.timeWithoutProgress = 0;
            this.totalTime = 0;
        }
        this.lastMatchNanos = now;

        this.totalTime += dt;
        if (distance < this.bestDistance - this.minProgress) {
            this.bestDistance = distance;
            this.timeWithoutProgress = 0;
        } else {
            this.timeWithoutProgress += dt;
        }

        if (this.timeWithoutProgress < this.stuckTime && this.totalTime < this.maxTime) return false;

        this.trackedTask = null;
        return true;
    }

    @Nullable
    private static Vector3d getOwnerPosition(@Nonnull HexCreatureMinionComponent minionComponent, @Nonnull Store<EntityStore> store) {
        Ref<EntityStore> ownerRef = store.getExternalData().getRefFromUUID(UUID.fromString(minionComponent.getOwnerUUID()));
        if (ownerRef == null || !ownerRef.isValid()) return null;

        TransformComponent ownerTransform = store.getComponent(ownerRef, TransformComponent.getComponentType());
        return ownerTransform != null ? ownerTransform.getPosition() : null;
    }

    @Override
    public InfoProvider getSensorInfo() {
        return null;
    }
}
