package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.modules.projectile.config.ProjectileConfig;
import com.hypixel.hytale.server.core.modules.projectile.interaction.ProjectileInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.lexem.hexcodeevoke.components.DeflectableProjectileComponent;
import org.joml.Vector3d;

import javax.annotation.Nonnull;

public class DeflectableProjectileInteraction extends ProjectileInteraction {
    private String deflectedConfig;
    private double deflectRange = 4.0;
    private float deflectedLifetime = 4.0f;
    private String deflectParticles;
    private String deflectSoundEventId;

    public static final BuilderCodec<DeflectableProjectileInteraction> CODEC = BuilderCodec
            .builder(DeflectableProjectileInteraction.class, DeflectableProjectileInteraction::new, ProjectileInteraction.CODEC)
            .documentation("Fires a projectile that a player can hit back with a primary attack. The deflected projectile belongs to that player and flies where that player aims.")
            .append(new KeyedCodec<>("DeflectedConfig", Codec.STRING),
                    (o, v) -> o.deflectedConfig = v,
                    o -> o.deflectedConfig)
            .addValidator(ProjectileConfig.VALIDATOR_CACHE.getValidator().late())
            .documentation("Projectile config launched in place of the original when it is deflected.")
            .add()
            .append(new KeyedCodec<>("DeflectRange", Codec.DOUBLE),
                    (o, v) -> o.deflectRange = v,
                    o -> o.deflectRange)
            .documentation("Distance from the eyes of the player within which an attack deflects the projectile.")
            .add()
            .append(new KeyedCodec<>("DeflectedLifetime", Codec.FLOAT),
                    (o, v) -> o.deflectedLifetime = v,
                    o -> o.deflectedLifetime)
            .documentation("Seconds the deflected projectile lives.")
            .add()
            .append(new KeyedCodec<>("DeflectParticles", Codec.STRING),
                    (o, v) -> o.deflectParticles = v,
                    o -> o.deflectParticles)
            .addValidator(ParticleSystem.VALIDATOR_CACHE.getValidator().late())
            .documentation("Particle system spawned where the projectile is deflected.")
            .add()
            .append(new KeyedCodec<>("DeflectSoundEventId", Codec.STRING),
                    (o, v) -> o.deflectSoundEventId = v,
                    o -> o.deflectSoundEventId)
            .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator().late())
            .documentation("Sound played where the projectile is deflected.")
            .add()
            .build();

    @Override
    protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        ProjectileConfig projectileConfig = getConfig();
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        if (projectileConfig == null || commandBuffer == null) return;

        Ref<EntityStore> owner = context.getOwningEntity();
        Ref<EntityStore> creatorRef = owner != null && owner.isValid() ? owner : context.getEntity();

        Transform look = TargetUtil.getLook(context.getEntity(), commandBuffer);
        Ref<EntityStore> projectileRef = ProjectileModule.get().spawnProjectile(
                null, null, creatorRef, commandBuffer, projectileConfig,
                new Vector3d(look.getPosition()), new Vector3d(look.getDirection()), (long) (lifetime * 1000));

        if (deflectedConfig == null || deflectedConfig.isEmpty()) return;

        DeflectableProjectileComponent deflectable = new DeflectableProjectileComponent(
                deflectedConfig, deflectRange, deflectedLifetime, deflectParticles, deflectSoundEventId);
        commandBuffer.run(store -> {
            if (projectileRef.isValid()) {
                store.putComponent(projectileRef, DeflectableProjectileComponent.getComponentType(), deflectable);
            }
        });
    }
}
