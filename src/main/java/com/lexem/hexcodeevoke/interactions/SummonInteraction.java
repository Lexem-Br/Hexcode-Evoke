package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.map.IWeightedElement;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.validators.NPCRoleValidator;
import it.unimi.dsi.fastutil.Pair;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadLocalRandom;

public class SummonInteraction extends EvokeSimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final BuilderCodec<SummonInteraction> CODEC =
            BuilderCodec.builder(SummonInteraction.class, SummonInteraction::new,
                            SimpleInteraction.CODEC)
                    .append(new KeyedCodec<>("EntityId", Codec.STRING),
                            (config, value) -> config.entityId = value,
                            (config) -> config.entityId)
                    .documentation("The ID of the entity asset to spawn.")
                    .addValidator(NPCRoleValidator.INSTANCE)
                    .add()
                    .append(new KeyedCodec<>("ParticleId", Codec.STRING),
                            (config, value) -> config.particleId = value,
                            (config) -> config.particleId)
                    .documentation("The ID of the particle to spawn.")
                    .add()
                    .append(new KeyedCodec<>("ParticleScale", Codec.DOUBLE),
                            (config, value) -> config.scale = value,
                            (config) -> config.scale)
                    .documentation("Scale of the particle.")
                    .add()
                    .append(new KeyedCodec<>("ParticleMaxDuration", Codec.DOUBLE),
                            (config, value) -> config.maxDuration = value,
                            (config) -> config.maxDuration)
                    .documentation("Max duration time of the particle.")
                    .add()
                    .<SummonInteraction.WeightedNPCSpawn[]>append(
                            new KeyedCodec<>("WeightedEntityIds", new ArrayCodec<>(SummonInteraction.WeightedNPCSpawn.CODEC, SummonInteraction.WeightedNPCSpawn[]::new)),
                            (summonInteraction, o) -> summonInteraction.weightedSpawns = o,
                            summonInteraction -> summonInteraction.weightedSpawns
                    )
                    .documentation("A weighted list of entity IDs from which an entity will be selected for spawning. Supersedes any provided EntityId.")
                    .add()
                    .afterDecode(interaction -> {
                        if (interaction.weightedSpawns != null && interaction.weightedSpawns.length > 0) {
                            WeightedMap.Builder<String> mapBuilder = WeightedMap.builder(ArrayUtil.EMPTY_STRING_ARRAY);

                            for (SummonInteraction.WeightedNPCSpawn entry : interaction.weightedSpawns) {
                                mapBuilder.put(entry.id, entry.weight);
                            }

                            interaction.weightedSpawnMap = mapBuilder.build();
                        }
                    })
                    .build();
    protected String entityId;
    protected String particleId = "MagicPortal_VoidKeyArt";
    private double scale = 1.0;
    private double maxDuration = 2.0;
    private static final double PORTAL_BEHIND_DISTANCE = 0.8;
    protected SummonInteraction.WeightedNPCSpawn[] weightedSpawns;
    protected IWeightedMap<String> weightedSpawnMap;

    @Override
    protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        try {
            CommandBuffer<EntityStore> accessor = context.getCommandBuffer();
            BlockPosition blockPosition = context.getTargetBlock();
            String entityToSpawn = this.weightedSpawnMap != null ? this.weightedSpawnMap.get(ThreadLocalRandom.current()) : this.entityId;
            int roleIndex = entityToSpawn != null ? NPCPlugin.get().getIndex(entityToSpawn) : -1;
            if (roleIndex < 0) {
                LOGGER.atWarning().log("[hexcode evoke] Unable to spawn %s: unknown NPC role", entityToSpawn);
            }

            boolean spawned = accessor != null && blockPosition != null && roleIndex >= 0 && trySpawn(blockPosition, entityToSpawn, roleIndex, accessor);
            complete(spawned, firstRun, time, type, context, cooldownHandler);
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode evoke] Spawn failed");
            context.getState().state = InteractionState.Failed;
        }
    }

    private boolean trySpawn(@Nonnull BlockPosition position, @Nonnull String entityId, int roleIndex, @Nonnull CommandBuffer<EntityStore> accessor) {
        World world = accessor.getExternalData().getWorld();
        Ref<ChunkStore> section = world.getChunkStore().getChunkSectionReferenceAtBlock(position.x, position.y, position.z);
        if (section == null) return false;

        BlockSection blockSection = section.getStore().getComponent(section, BlockSection.getComponentType());
        if (blockSection == null) return false;

        int blockRotationIndex = blockSection.getRotationIndex(position.x, position.y, position.z);
        RotationTuple rotation = RotationTuple.get(blockRotationIndex);
        Rotation3f blockRotation = new Rotation3f(0.0F, (float) (rotation.yaw().getRadians() + Math.PI), 0.0F);

        accessor.run(store -> {
            Vector3d blockVector = new Vector3d(position.x, position.y + 1, position.z);
            Pair<Ref<EntityStore>, NPCEntity> npcPair = NPCPlugin.get().spawnEntity(store, roleIndex, blockVector, blockRotation, null, null);
            if (npcPair == null) {
                LOGGER.atWarning().log("[hexcode evoke] Unable to spawn %s at %d %d %d", entityId, position.x, position.y, position.z);
                return;
            }
            this.spawnPortalParticleEffect(npcPair.first(), store, blockRotation);
        });

        return true;
    }

    private void spawnPortalParticleEffect(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull Rotation3f rotation) {
        TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
        if (transformComponent == null) return;

        float eyeHeight = 0.0F;
        ModelComponent modelComponent = store.getComponent(ref, ModelComponent.getComponentType());
        if (modelComponent != null) {
            eyeHeight = modelComponent.getModel().getEyeHeight(ref, store);
        }

        float yaw = rotation.yaw();
        Vector3d particlePos = new Vector3d(transformComponent.getPosition())
                .add(Math.sin(yaw) * PORTAL_BEHIND_DISTANCE, eyeHeight, Math.cos(yaw) * PORTAL_BEHIND_DISTANCE);

        ParticleUtil.spawnParticleEffect(
                this.particleId,
                particlePos,
                (float) (yaw - Math.PI),
                rotation.x(),
                rotation.z(),
                (float) this.scale,
                (float) this.maxDuration,
                store
        );
    }

    protected static class WeightedNPCSpawn implements IWeightedElement {
        private static final BuilderCodec<SummonInteraction.WeightedNPCSpawn> CODEC = BuilderCodec.builder(
                        SummonInteraction.WeightedNPCSpawn.class, SummonInteraction.WeightedNPCSpawn::new
                )
                .append(new KeyedCodec<>("Id", Codec.STRING), (spawn, s) -> spawn.id = s, spawn -> spawn.id)
                .documentation("The Role ID of the NPC to spawn.")
                .addValidator(Validators.nonNull())
                .addValidator(NPCRoleValidator.INSTANCE)
                .add()
                .<Double>append(new KeyedCodec<>("Weight", Codec.DOUBLE, true), (spawn, d) -> spawn.weight = d, spawn -> spawn.weight)
                .documentation("The relative weight of this NPC (chance of being spawned is this value relative to the sum of all weights).")
                .addValidator(Validators.nonNull())
                .addValidator(Validators.greaterThan(0.0))
                .add()
                .build();
        private String id;
        private double weight;

        private WeightedNPCSpawn() {
        }

        @Override
        public double getWeight() {
            return this.weight;
        }
    }
}
