package com.lexem.hexcodeevoke.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.entity.DespawnComponent;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.InteractionSimulationHandler;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.lexem.hexcodeevoke.components.DeflectableProjectileComponent;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ProjectileDeflectSystem extends EntityTickingSystem<EntityStore> {
    private static final float SWING_WINDOW = 0.3f;
    private static final double MIN_AIM_DOT = 0.35;
    private static final double AIM_DISTANCE = 20.0;

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        DeflectableProjectileComponent deflectable = archetypeChunk.getComponent(index, DeflectableProjectileComponent.getComponentType());
        TransformComponent transform = archetypeChunk.getComponent(index, TransformComponent.getComponentType());
        if (deflectable == null || transform == null) return;

        Vector3d center = new Vector3d(transform.getPosition());
        BoundingBox boundingBox = archetypeChunk.getComponent(index, BoundingBox.getComponentType());
        if (boundingBox != null) {
            center.y += (boundingBox.getBoundingBox().min.y + boundingBox.getBoundingBox().max.y) * 0.5;
        }

        double range = deflectable.getRange();
        List<Ref<EntityStore>> players = SpatialResource.getThreadLocalReferenceList();
        store.getResource(EntityModule.get().getPlayerSpatialResourceType())
                .getSpatialStructure().collect(center, range + 2.0, players);

        for (Ref<EntityStore> playerRef : players) {
            if (playerRef == null || !playerRef.isValid()) continue;
            if (store.getArchetype(playerRef).contains(DeathComponent.getComponentType())) continue;
            if (!isSwinging(playerRef, store)) continue;

            Transform look = TargetUtil.getLook(playerRef, store);
            Vector3d eye = look.getPosition();
            Vector3d lookDirection = new Vector3d(look.getDirection()).normalize();
            Vector3d toProjectile = new Vector3d(center).sub(eye);
            double distance = toProjectile.length();
            if (distance > range) continue;
            if (distance > 0.5 && toProjectile.dot(lookDirection) / distance < MIN_AIM_DOT) continue;

            deflect(archetypeChunk.getReferenceTo(index), playerRef, deflectable, transform.getPosition(), eye, lookDirection, commandBuffer);
            return;
        }
    }

    private static boolean isSwinging(@Nonnull Ref<EntityStore> playerRef, @Nonnull Store<EntityStore> store) {
        InteractionManager interactionManager = store.getComponent(playerRef, InteractionModule.get().getInteractionManagerComponent());
        if (interactionManager == null) return false;

        for (InteractionChain chain : interactionManager.getChains().values()) {
            if (chain.getType() == InteractionType.Primary && chain.getTimeInSeconds() <= SWING_WINDOW) {
                return true;
            }
        }
        return false;
    }

    private static void deflect(
            @Nonnull Ref<EntityStore> projectileRef,
            @Nonnull Ref<EntityStore> playerRef,
            @Nonnull DeflectableProjectileComponent deflectable,
            @Nonnull Vector3d projectilePosition,
            @Nonnull Vector3d eye,
            @Nonnull Vector3d lookDirection,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        ProjectileConfig config = ProjectileConfig.getAssetMap().getAsset(deflectable.getDeflectedConfig());
        if (config == null) return;

        Vector3d position = new Vector3d(projectilePosition);
        Vector3d direction = new Vector3d(lookDirection).mul(AIM_DISTANCE).add(eye).sub(position);
        if (direction.lengthSquared() < 1.0E-6 || direction.dot(lookDirection) <= 0) {
            direction.set(lookDirection);
        }
        direction.normalize();

        commandBuffer.removeEntity(projectileRef, RemoveReason.REMOVE);

        Vector3d effectPosition = new Vector3d(position);
        Ref<EntityStore> deflectedRef = ProjectileModule.get().spawnProjectile(playerRef, commandBuffer, config, position, direction);
        float lifetime = deflectable.getLifetime();
        commandBuffer.run(entityStore -> {
            if (!deflectedRef.isValid()) return;
            entityStore.putComponent(deflectedRef, InteractionModule.get().getInteractionManagerComponent(),
                    new InteractionManager(null, new InteractionSimulationHandler()));
            if (lifetime > 0) {
                entityStore.putComponent(deflectedRef, DespawnComponent.getComponentType(),
                        DespawnComponent.despawnInSeconds(entityStore.getResource(TimeResource.getResourceType()), lifetime));
            }
        });

        String particles = deflectable.getParticles();
        if (particles != null && !particles.isEmpty()) {
            ParticleUtil.spawnParticleEffect(particles, effectPosition, commandBuffer);
        }

        String soundEventId = deflectable.getSoundEventId();
        if (soundEventId != null && !soundEventId.isEmpty()) {
            int soundIndex = SoundEvent.getAssetMap().getIndex(soundEventId);
            if (soundIndex != SoundEvent.EMPTY_ID) {
                SoundUtil.playSoundEvent3d(soundIndex, SoundCategory.SFX, effectPosition, commandBuffer);
            }
        }
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(DeflectableProjectileComponent.getComponentType(), TransformComponent.getComponentType());
    }
}
