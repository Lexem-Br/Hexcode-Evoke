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
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
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
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SummonInteraction extends SimpleInteraction {
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
    protected SummonInteraction.WeightedNPCSpawn[] weightedSpawns;
    protected IWeightedMap<String> weightedSpawnMap;

    @Override
    protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        try {
            CommandBuffer<EntityStore> accessor = context.getCommandBuffer();
            if (accessor == null) {
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            BlockPosition blockPosition = context.getTargetBlock();
            if (blockPosition == null) {
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            String entityToSpawn = this.entityId;
            if (this.weightedSpawnMap != null) {
                entityToSpawn = this.weightedSpawnMap.get(ThreadLocalRandom.current());
            }

            boolean spawned = trySpawn(blockPosition, entityToSpawn, accessor, context);
            if (!spawned) {
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
            }

            context.getState().state = InteractionState.Finished;
            super.tick0(firstRun, time, type, context, cooldownHandler);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode evoke] Spawn failed: %s", e.getMessage());
            context.getState().state = InteractionState.Failed;
        }
    }

    public boolean trySpawn(BlockPosition position, String entityId, CommandBuffer<EntityStore> accessor, InteractionContext context) {
        World world = accessor.getExternalData().getWorld();
        Ref<ChunkStore> section = world.getChunkStore().getChunkSectionReferenceAtBlock(position.x, position.y, position.z);
        if (section == null) return false;

        BlockSection blockSection = section.getStore().getComponent(section, BlockSection.getComponentType());
        if (blockSection == null) return false;

        int blockRotationIndex = blockSection.getRotationIndex(position.x, position.y, position.z);
        RotationTuple rotation = RotationTuple.get(blockRotationIndex);
        Rotation3f blockRotation = new Rotation3f(0.0F, (float) (rotation.yaw().getRadians() + Math.PI), 0.0F);

        accessor.run(_store -> {
            int roleIndex = NPCPlugin.get().getIndex(entityId);
            if (roleIndex >= 0) {
                Vector3d blockVector = new Vector3d(position.x, position.y + 1, position.z);
                Pair<Ref<EntityStore>, NPCEntity> npcPair =NPCPlugin.get().spawnEntity(_store, roleIndex, blockVector, blockRotation, null, null);
                if (npcPair == null) {
                    context.getState().state = InteractionState.Failed;
                } else {
                    this.spawnPortalParticleEffect(npcPair.first(), context.getEntity().getStore(), blockRotation);
                }
            }  else {
                context.getState().state = InteractionState.Failed;
                LOGGER.atWarning().log("Unable to spawn entity");
            }
        });

        return true;
    }

    public void spawnPortalParticleEffect(@Nonnull Ref<EntityStore> ref, Store<EntityStore> store, Rotation3f rotation) {
        TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
        if (transformComponent != null) {
            float eyeHeight = 0.0F;
            ModelComponent modelComponent = store.getComponent(ref, ModelComponent.getComponentType());
            if (modelComponent != null) {
                eyeHeight = modelComponent.getModel().getEyeHeight(ref, store);
            }

            //TODO:Add pitch

            Vector3d particlePos = new Vector3d(transformComponent.getPosition());
            particlePos.add(0.0F, eyeHeight, -0.8F);

            SpatialResource<Ref<EntityStore>, EntityStore> playerSpatialResource = store.getResource(
                    EntityModule.get().getPlayerSpatialResourceType()
            );
            List<Ref<EntityStore>> results = SpatialResource.getThreadLocalReferenceList();
            playerSpatialResource.getSpatialStructure().collect(particlePos, 75.0, results);

            ParticleUtil.spawnParticleEffect(
                    this.particleId,
                    particlePos,
                    (float)Math.toRadians(rotation.x),
                    (float)Math.toRadians(rotation.y),
                    (float)Math.toRadians(rotation.z),
                    (float)this.scale,
                    (float)maxDuration,
                    store
            );
        }
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
