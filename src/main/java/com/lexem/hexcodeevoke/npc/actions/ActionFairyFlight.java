package com.lexem.hexcodeevoke.npc.actions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.sensorinfo.IPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.npc.actions.builders.BuilderActionFairyFlight;
import com.lexem.hexcodeevoke.utils.FairyFlightSettings;
import com.lexem.hexcodeevoke.utils.FairyFlightState;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ActionFairyFlight extends ActionBase {
    private static final double MAX_STEP_SECONDS = 0.1;
    private static final double MAX_SUBSTEP = 0.3;
    private static final double MIN_MOVE = 1.0E-6;
    private static final double PHASE_PROBE_DISTANCE = 0.4;
    private static final double BODY_CENTER_HEIGHT = 0.3;
    private static final double EVOKER_HEAD_HEIGHT = 1.6;
    private static final double TRAIL_RANGE = 48.0;

    private final FairyFlightSettings settings;
    private final boolean hover;
    private final boolean ignoreBlocks;
    @Nullable
    private final String trailParticle;
    private final double trailMinSpeed;
    private final double trailInterval;
    private final Vector3d target = new Vector3d();
    private final Vector3d candidate = new Vector3d();
    private final Vector3d start = new Vector3d();
    private final Vector3d evokerHead = new Vector3d();

    public ActionFairyFlight(@Nonnull BuilderActionFairyFlight builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.settings = builder.getSettings(support);
        this.hover = builder.isHover();
        this.ignoreBlocks = builder.isIgnoreBlocks();
        this.trailParticle = builder.getTrailParticle(support);
        this.trailMinSpeed = builder.getTrailMinSpeed(support);
        this.trailInterval = builder.getTrailInterval(support);
    }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> npcRef, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(npcRef, executionSupport, sensorInfo, dt, store);

        HexCreatureComponent hexCreature = store.getComponent(npcRef, HexCreatureComponent.getComponentType());
        TransformComponent transform = store.getComponent(npcRef, TransformComponent.getComponentType());
        if (hexCreature == null || transform == null || dt <= 0) return false;

        Vector3d position = transform.getPosition();
        FairyFlightState flight = hexCreature.getFlightState();
        if (!flight.isActive()) {
            flight.begin(transform.getRotation().yaw(), transform.getRotation().pitch(), transform.getRotation().roll());
        }

        if (this.hover) {
            this.target.set(flight.holdAnchor(position, this.settings.acceleration()));
        } else {
            IPositionProvider positionProvider = sensorInfo != null ? sensorInfo.getPositionProvider() : null;
            if (positionProvider == null || !positionProvider.providePosition(this.target)) return false;
            flight.clearAnchor();
        }

        double step = Math.min(dt, MAX_STEP_SECONDS);
        Ref<EntityStore> evokerRef = this.getEvokerRef(hexCreature, store);
        TransformComponent evokerTransform = evokerRef != null ? store.getComponent(evokerRef, TransformComponent.getComponentType()) : null;
        Vector3dc head = null;
        Float evokerLookYaw = null;
        boolean calm = false;
        if (evokerTransform != null) {
            Vector3d evokerPosition = evokerTransform.getPosition();
            head = this.evokerHead.set(evokerPosition.x, evokerPosition.y + EVOKER_HEAD_HEIGHT - BODY_CENTER_HEIGHT, evokerPosition.z);
            HeadRotation evokerHeadRotation = store.getComponent(evokerRef, HeadRotation.getComponentType());
            evokerLookYaw = evokerHeadRotation != null ? evokerHeadRotation.getRotation().yaw() : evokerTransform.getRotation().yaw();
            MovementStatesComponent evokerStates = store.getComponent(evokerRef, MovementStatesComponent.getComponentType());
            calm = evokerStates != null && evokerStates.getMovementStates().crouching;
        }

        flight.steer(this.settings, position, this.target, head, calm, step);
        this.move(flight, position, step, executionSupport.getMotionContextSupport().getActiveMotionController(), store);
        flight.updatePose(this.settings, evokerLookYaw, step);
        flight.markActive();

        if (this.trailParticle != null && flight.shouldEmitTrail(this.trailMinSpeed, this.trailInterval, step)) {
            this.emitTrail(position, store);
        }
        return true;
    }

    private void move(@Nonnull FairyFlightState flight, @Nonnull Vector3d position, double dt, @Nullable MotionController motionController, @Nonnull Store<EntityStore> store) {
        Vector3d velocity = flight.getVelocity();
        double length = velocity.length() * dt;
        if (this.ignoreBlocks || motionController == null) {
            flight.stopPhasing();
            position.add(velocity.x * dt, velocity.y * dt, velocity.z * dt);
            return;
        }

        if (length < MIN_MOVE) {
            flight.onMoved(false, false, 1, dt);
            return;
        }

        if (flight.isPhasing()) {
            position.add(velocity.x * dt, velocity.y * dt, velocity.z * dt);
            this.candidate.set(velocity).normalize(PHASE_PROBE_DISTANCE).add(position);
            flight.updatePhasing(motionController.isValidPosition(position, store) && motionController.isValidPosition(this.candidate, store), dt);
            return;
        }

        if (!motionController.isValidPosition(position, store)) {
            position.add(velocity.x * dt, velocity.y * dt, velocity.z * dt);
            return;
        }

        this.start.set(position);
        int steps = Math.max(1, (int) Math.ceil(length / MAX_SUBSTEP));
        double stepX = velocity.x * dt / steps;
        double stepY = velocity.y * dt / steps;
        double stepZ = velocity.z * dt / steps;
        boolean blocked = false;
        boolean blockedSideways = false;

        for (int i = 0; i < steps; i++) {
            this.candidate.set(position).add(stepX, stepY, stepZ);
            if (motionController.isValidPosition(this.candidate, store)) {
                position.set(this.candidate);
                continue;
            }

            blocked = true;
            if (stepX != 0) {
                this.candidate.set(position.x + stepX, position.y, position.z);
                if (motionController.isValidPosition(this.candidate, store)) {
                    position.x = this.candidate.x;
                } else {
                    stepX = 0;
                    velocity.x = 0;
                    blockedSideways = true;
                }
            }
            if (stepZ != 0) {
                this.candidate.set(position.x, position.y, position.z + stepZ);
                if (motionController.isValidPosition(this.candidate, store)) {
                    position.z = this.candidate.z;
                } else {
                    stepZ = 0;
                    velocity.z = 0;
                    blockedSideways = true;
                }
            }
            if (stepY != 0) {
                this.candidate.set(position.x, position.y + stepY, position.z);
                if (motionController.isValidPosition(this.candidate, store)) {
                    position.y = this.candidate.y;
                } else {
                    stepY = 0;
                    velocity.y = 0;
                }
            }
        }

        flight.onMoved(blocked, blockedSideways, position.distance(this.start) / length, dt);
    }

    private void emitTrail(@Nonnull Vector3d position, @Nonnull Store<EntityStore> store) {
        this.candidate.set(position.x, position.y + BODY_CENTER_HEIGHT, position.z);

        SpatialResource<Ref<EntityStore>, EntityStore> players = store.getResource(EntityModule.get().getPlayerSpatialResourceType());
        List<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
        players.getSpatialStructure().collect(this.candidate, TRAIL_RANGE, results);
        if (results.isEmpty()) return;

        ParticleUtil.spawnParticleEffect(this.trailParticle, this.candidate.x, this.candidate.y, this.candidate.z, 0f, 0f, 0f, 1f, null, null, results, store);
    }

    @Nullable
    private Ref<EntityStore> getEvokerRef(@Nonnull HexCreatureComponent hexCreature, @Nonnull Store<EntityStore> store) {
        if (hexCreature.getEvokerUUID() == null) return null;

        Ref<EntityStore> evokerRef = store.getExternalData().getRefFromUUID(UUID.fromString(hexCreature.getEvokerUUID()));
        return evokerRef != null && evokerRef.isValid() ? evokerRef : null;
    }
}
