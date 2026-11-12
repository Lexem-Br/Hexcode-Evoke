package com.lexem.hexcodeevoke.npc.actions.voidarena;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.lexem.hexcodeevoke.npc.actions.voidarena.builders.BuilderActionVoidArenaBlink;
import com.lexem.hexcodeevoke.utils.BlockUtils;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionVoidArenaBlink extends ActionBase {
    private static final String PARTICLES = "Evoke_Void_Teleport";
    private static final double PARTICLE_HEIGHT = 1.0;
    private static final float PARTICLE_SCALE = 0.6F;
    private static final int CANDIDATES = 48;
    private static final double SIGHT_HEIGHT = 1.5;
    private static final double SIGHT_STEP = 0.5;

    private final double minDistance;
    private final double maxDistance;
    private final double maxYOffset;

    public ActionVoidArenaBlink(@Nonnull BuilderActionVoidArenaBlink builder, @Nonnull BuilderSupport support) {
        super(builder);
        double[] distance = builder.getDistanceRange(support);
        this.minDistance = distance[0];
        this.maxDistance = distance[1];
        this.maxYOffset = builder.getMaxYOffset();
    }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        super.execute(ref, executionSupport, sensorInfo, dt, store);

        TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
        MotionController motionController = executionSupport.getMotionContextSupport().getActiveMotionController();
        if (transform == null || motionController == null || store.getComponent(ref, Teleport.getComponentType()) != null) return true;

        Vector3d origin = new Vector3d(transform.getPosition());
        Vector3d destination = findDestination(origin, motionController, ref, store);
        if (destination == null) return true;

        Rotation3f rotation = transform.getRotation();
        store.addComponent(ref, Teleport.getComponentType(), Teleport.createExact(destination, facePlayer(destination, rotation, store)));
        NPCEntity npc = store.getComponent(ref, NPCEntity.getComponentType());
        if (npc != null) {
            npc.storeTickStartPosition(destination);
            npc.setCurrentFallDistance(0.0);
        }
        spawnParticles(origin, store);
        spawnParticles(destination, store);
        return true;
    }

    @Nullable
    private Vector3d findDestination(@Nonnull Vector3d origin, @Nonnull MotionController motionController, @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        World world = store.getExternalData().getWorld();
        BoundingBox boundingBox = store.getComponent(ref, BoundingBox.getComponentType());
        double idealDistance = Math.max(maxDistance, nearestPlayerDistance(origin, store));

        Vector3d best = null;
        double bestError = Double.MAX_VALUE;
        for (int i = 0; i < CANDIDATES; i++) {
            double angle = RandomExtra.randomRange(0.0, Math.PI * 2.0);
            double distance = RandomExtra.randomRange(minDistance, maxDistance);
            Vector3d candidate = new Vector3d(origin.x + Math.cos(angle) * distance, origin.y, origin.z + Math.sin(angle) * distance);

            boolean accessible = motionController.translateToAccessiblePosition(candidate, boundingBox != null ? boundingBox.getBoundingBox() : null, origin.y - maxYOffset, origin.y + maxYOffset, store);
            if (!accessible || !motionController.isValidPosition(candidate, store) || !hasClearPath(world, origin, candidate)) continue;

            double error = Math.abs(nearestPlayerDistance(candidate, store) - idealDistance);
            if (error < bestError) {
                bestError = error;
                best = candidate;
            }
        }
        return best;
    }

    private static double nearestPlayerDistance(@Nonnull Vector3d position, @Nonnull Store<EntityStore> store) {
        Vector3d player = nearestPlayer(position, store);
        return player != null ? player.distance(position) : 0.0;
    }

    @Nullable
    private static Vector3d nearestPlayer(@Nonnull Vector3d position, @Nonnull Store<EntityStore> store) {
        Vector3d nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (PlayerRef playerRef : store.getExternalData().getWorld().getPlayerRefs()) {
            Ref<EntityStore> ref = playerRef.getReference();
            if (ref == null || !ref.isValid()) continue;

            TransformComponent transform = store.getComponent(ref, TransformComponent.getComponentType());
            if (transform == null) continue;

            double distance = transform.getPosition().distanceSquared(position);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = new Vector3d(transform.getPosition());
            }
        }
        return nearest;
    }

    private static boolean hasClearPath(@Nonnull World world, @Nonnull Vector3d from, @Nonnull Vector3d to) {
        int steps = (int) Math.ceil(from.distance(to) / SIGHT_STEP);
        Vector3i block = new Vector3i();
        for (int i = 1; i <= steps; i++) {
            double progress = (double) i / steps;
            block.set(
                    (int) Math.floor(from.x + (to.x - from.x) * progress),
                    (int) Math.floor(from.y + (to.y - from.y) * progress + SIGHT_HEIGHT),
                    (int) Math.floor(from.z + (to.z - from.z) * progress));
            if (BlockUtils.getBlockTypeByPosition(block, world).getMaterial() == BlockMaterial.Solid) return false;
        }
        return true;
    }

    @Nonnull
    private static Rotation3f facePlayer(@Nonnull Vector3d destination, @Nonnull Rotation3f rotation, @Nonnull Store<EntityStore> store) {
        Vector3d player = nearestPlayer(destination, store);
        if (player == null) return rotation;

        float yaw = PhysicsMath.normalizeTurnAngle(PhysicsMath.headingFromDirection(player.x - destination.x, player.z - destination.z));
        return new Rotation3f(rotation.pitch(), yaw, rotation.roll());
    }

    private static void spawnParticles(@Nonnull Vector3d position, @Nonnull Store<EntityStore> store) {
        ParticleUtil.spawnParticleEffect(PARTICLES, new Vector3d(position).add(0.0, PARTICLE_HEIGHT, 0.0), 0.0F, 0.0F, 0.0F, PARTICLE_SCALE, 1.0F, store);
    }
}
