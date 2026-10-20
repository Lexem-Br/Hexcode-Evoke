package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.StateMappingHelper;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.support.StateSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionUnstuck;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ActionUnstuck extends ActionBase {
    private static final String SEEK_POSITION_STATE = "SeekPosition";
    private static final double FREE_SEARCH_STEP = 0.25;
    private static final double MIN_FREE_HEIGHT = 0.25;
    private static final long RESTART_GAP_NANOS = 250_000_000L;
    private static final double CLOSE_DISTANCE = 1.0;
    private static final double START_LIFT = 0.3;
    private static final double START_SPEED_FACTOR = 0.6;

    protected final double speed;
    protected final double heightOffset;
    protected final double turnRate;
    protected final double startSpread;
    private final Vector3d target = new Vector3d();
    private final Vector3d direction = new Vector3d();
    private final Vector3d desired = new Vector3d();
    private final Vector3d velocity = new Vector3d();
    private long lastExecuteNanos;

    public ActionUnstuck(@Nonnull BuilderActionUnstuck builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.speed = builder.getSpeed(support);
        this.heightOffset = builder.getHeightOffset(support);
        this.turnRate = builder.getTurnRate(support);
        this.startSpread = builder.getStartSpread(support);
    }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, executionSupport, sensorInfo, dt, store);

        TransformComponent transformComponent = store.getComponent(npcRef, TransformComponent.getComponentType());
        if (transformComponent == null || !this.findTarget(npcRef, executionSupport, store)) return false;

        Vector3d position = transformComponent.getPosition();
        this.direction.set(this.target).sub(position);
        double distance = this.direction.length();
        if (distance < 1.0E-3) return false;

        long now = System.nanoTime();
        boolean starting = now - this.lastExecuteNanos > RESTART_GAP_NANOS;
        this.lastExecuteNanos = now;

        if (this.turnRate <= 0) {
            this.velocity.set(this.direction).mul(this.speed / distance);
        } else {
            if (starting) this.startCurve(transformComponent, distance);

            // Turns the current velocity towards the target a bit each tick, which bends the path into a curve
            double blend = distance < CLOSE_DISTANCE ? 1.0 : Math.min(1.0, this.turnRate * dt);
            this.desired.set(this.direction).mul(this.speed / distance);
            this.velocity.lerp(this.desired, blend);
        }

        double step = Math.min(this.velocity.length() * dt, distance);
        if (step < 1.0E-6) return false;

        if (this.velocity.x * this.velocity.x + this.velocity.z * this.velocity.z > 1.0E-4) {
            transformComponent.getRotation().setYaw(PhysicsMath.headingFromDirection(this.velocity.x, this.velocity.z));
        }

        this.direction.set(this.velocity).normalize(step);
        position.add(this.direction);
        return true;
    }

    private void startCurve(@Nonnull TransformComponent transformComponent, double distance) {
        double spread = Math.toRadians(this.startSpread) * (ThreadLocalRandom.current().nextBoolean() ? 1 : -1);
        double cos = Math.cos(spread);
        double sin = Math.sin(spread);

        // Starts from the direction to the target turned to a random side, lifted slightly
        double x = this.direction.x / distance;
        double z = this.direction.z / distance;
        this.velocity.set(x * cos - z * sin, this.direction.y / distance + START_LIFT, x * sin + z * cos);
        this.velocity.normalize(this.speed * START_SPEED_FACTOR);
    }

    private boolean findTarget(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nonnull Store<EntityStore> store) {
        HexCreatureComponent hexCreature = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        if (hexCreature == null || hexCreature.getEvokerUUID() == null) return false;

        Ref<EntityStore> evokerRef = store.getExternalData().getRefFromUUID(UUID.fromString(hexCreature.getEvokerUUID()));
        if (evokerRef == null || !evokerRef.isValid()) return false;

        Vector3d basePosition = null;
        if (this.isSeekingPosition(npcRef, store)) {
            EvokerComponent evoker = store.getComponent(evokerRef, EvokerComponent.getComponentType());
            if (evoker != null && !evoker.getTargetPosition().equals(Vector3dUtil.MIN)) {
                basePosition = evoker.getTargetPosition();
            }
        }

        if (basePosition == null) {
            TransformComponent evokerTransform = store.getComponent(evokerRef, TransformComponent.getComponentType());
            if (evokerTransform == null) return false;
            basePosition = evokerTransform.getPosition();
        }

        MotionController motionController = executionSupport.getMotionContextSupport().getActiveMotionController();
        for (double height = this.heightOffset; height >= MIN_FREE_HEIGHT; height -= FREE_SEARCH_STEP) {
            this.target.set(basePosition.x, basePosition.y + height, basePosition.z);
            if (motionController == null || motionController.isValidPosition(this.target, store)) break;
        }
        return true;
    }

    private boolean isSeekingPosition(@Nonnull Ref<EntityStore> npcRef, @Nonnull Store<EntityStore> store) {
        StateSupport stateSupport = store.getComponent(npcRef, StateSupport.getComponentType());
        if (stateSupport == null) return false;

        StateMappingHelper stateHelper = stateSupport.getStateHelper();
        return stateHelper != null && SEEK_POSITION_STATE.equals(stateHelper.getStateName(stateSupport.getStateIndex()));
    }
}
