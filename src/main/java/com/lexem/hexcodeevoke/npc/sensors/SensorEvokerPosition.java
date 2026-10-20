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
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorEvokerPosition;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class SensorEvokerPosition extends SensorBase {
    private final double minRange;
    private final double range;
    private final double heightOffset;
    private final double leadDistance;
    private final double leadFullSpeed;
    private final double leadSideOffset;
    private final PositionProvider positionProvider = new PositionProvider();

    private static final double VELOCITY_SMOOTHING_SECONDS = 0.25;
    private static final double MAX_SAMPLE_GAP_SECONDS = 0.5;
    private static final double MAX_TRACKED_SPEED = 30.0;
    private static final double MIN_LEAD_SPEED = 0.5;
    private static final double SIDE_SWITCH_DISTANCE = 0.75;
    private static final double FREE_SEARCH_STEP = 0.5;
    private static final double MIN_FREE_HEIGHT = 0.25;

    private final Vector3d smoothedVelocity = new Vector3d();
    private int currentSide = 1;
    @Nullable
    private Vector3d lastEvokerPosition;
    private long lastUpdateNanos;

    public SensorEvokerPosition(@Nonnull BuilderSensorEvokerPosition builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.minRange = builder.getMinRange(support);
        this.range = builder.getRange(support);
        this.heightOffset = builder.getHeightOffset(support);
        this.leadDistance = builder.getLeadDistance(support);
        this.leadFullSpeed = builder.getLeadFullSpeed(support);
        this.leadSideOffset = builder.getLeadSideOffset(support);
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull ExecutionSupport executionSupport, double dt, @Nonnull Store<EntityStore> store) {
        if (!super.matches(ref, executionSupport, dt, store)) {
            this.positionProvider.clear();
            return false;
        }

        Ref<EntityStore> evokerRef = getEvokerRef(ref, store);
        TransformComponent evokerTransform = evokerRef != null ? store.getComponent(evokerRef, TransformComponent.getComponentType()) : null;
        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        if (evokerTransform == null || transform == null) {
            this.positionProvider.clear();
            return false;
        }

        Vector3d evokerPosition = evokerTransform.getPosition();
        updateEvokerVelocity(evokerPosition);

        Vector3d target = new Vector3d(evokerPosition).add(0, heightOffset, 0);
        double distanceSquared = transform.getPosition().distanceSquared(target);
        if (distanceSquared > range * range || distanceSquared < minRange * minRange) {
            this.positionProvider.clear();
            return false;
        }

        MotionController motionController = executionSupport.getMotionContextSupport().getActiveMotionController();
        if (motionController != null) {
            target = findFreeTarget(motionController, evokerPosition, getLeadOffset(transform.getPosition(), evokerPosition), store);
        } else {
            target.add(getLeadOffset(transform.getPosition(), evokerPosition));
        }

        this.positionProvider.setTarget(target);
        return true;
    }

    @Nonnull
    private Vector3d findFreeTarget(@Nonnull MotionController motionController, @Nonnull Vector3d evokerPosition, @Nonnull Vector3d leadOffset, @Nonnull Store<EntityStore> store) {
        Vector3d base = new Vector3d(evokerPosition).add(0, heightOffset, 0);
        for (double height = heightOffset; height >= MIN_FREE_HEIGHT; height -= FREE_SEARCH_STEP) {
            base.set(evokerPosition.x, evokerPosition.y + height, evokerPosition.z);
            if (motionController.isValidPosition(base, store)) break;
        }

        double leadLength = leadOffset.length();
        if (leadLength < FREE_SEARCH_STEP) return base.add(leadOffset);

        int steps = (int) Math.ceil(leadLength / FREE_SEARCH_STEP);
        Vector3d candidate = new Vector3d();
        for (int step = 1; step <= steps; step++) {
            candidate.set(leadOffset).mul((double) step / steps).add(base);
            if (!motionController.isValidPosition(candidate, store)) {
                return candidate.set(leadOffset).mul(Math.max(0, step - 2) / (double) steps).add(base);
            }
        }

        return base.add(leadOffset);
    }

    private void updateEvokerVelocity(@Nonnull Vector3d evokerPosition) {
        long now = System.nanoTime();
        double elapsed = (now - lastUpdateNanos) / 1_000_000_000.0;

        if (lastEvokerPosition == null || elapsed <= 0 || elapsed > MAX_SAMPLE_GAP_SECONDS) {
            smoothedVelocity.set(0, 0, 0);
        } else {
            double velocityX = (evokerPosition.x - lastEvokerPosition.x) / elapsed;
            double velocityZ = (evokerPosition.z - lastEvokerPosition.z) / elapsed;

            if (velocityX * velocityX + velocityZ * velocityZ > MAX_TRACKED_SPEED * MAX_TRACKED_SPEED) {
                smoothedVelocity.set(0, 0, 0);
            } else {
                double blend = 1 - Math.exp(-elapsed / VELOCITY_SMOOTHING_SECONDS);
                smoothedVelocity.x += (velocityX - smoothedVelocity.x) * blend;
                smoothedVelocity.z += (velocityZ - smoothedVelocity.z) * blend;
            }
        }

        lastEvokerPosition = new Vector3d(evokerPosition);
        lastUpdateNanos = now;
    }

    @Nonnull
    private Vector3d getLeadOffset(@Nonnull Vector3d creaturePosition, @Nonnull Vector3d evokerPosition) {
        double speed = Math.sqrt(smoothedVelocity.x * smoothedVelocity.x + smoothedVelocity.z * smoothedVelocity.z);
        if (leadDistance <= 0 || speed < MIN_LEAD_SPEED) return new Vector3d();

        double factor = Math.min(speed / leadFullSpeed, 1.0);
        double dirX = smoothedVelocity.x / speed;
        double dirZ = smoothedVelocity.z / speed;
        double sideX = -dirZ;
        double sideZ = dirX;
        double sideDistance = (creaturePosition.x - evokerPosition.x) * sideX + (creaturePosition.z - evokerPosition.z) * sideZ;
        if (sideDistance * currentSide < -SIDE_SWITCH_DISTANCE) {
            currentSide = -currentSide;
        }

        double lead = leadDistance * factor;
        double side = leadSideOffset * factor * currentSide;
        return new Vector3d(dirX * lead + sideX * side, 0, dirZ * lead + sideZ * side);
    }

    @Nullable
    private Ref<EntityStore> getEvokerRef(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        HexCreatureComponent hexCreature = store.getComponent(ref, HexCreatureComponent.getComponentType());
        if (hexCreature == null || hexCreature.getEvokerUUID() == null) return null;

        Ref<EntityStore> evokerRef = store.getExternalData().getRefFromUUID(UUID.fromString(hexCreature.getEvokerUUID()));
        return evokerRef != null && evokerRef.isValid() ? evokerRef : null;
    }

    @Override
    public InfoProvider getSensorInfo() {
        return this.positionProvider;
    }
}
