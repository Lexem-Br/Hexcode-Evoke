package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.lexem.hexcodeevoke.hexitems.HexItemRegistery;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class EvokeHexCreatureInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final BuilderCodec<EvokeHexCreatureInteraction> CODEC =
            BuilderCodec.builder(EvokeHexCreatureInteraction.class, EvokeHexCreatureInteraction::new,
                            SimpleInteraction.CODEC)
                    .build();

    protected int range = 2     ;
    protected int maxCount = 1;

    @Override
    protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type,
                         @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        Ref<EntityStore> owningEntity = context.getOwningEntity();
        Store<EntityStore> store = owningEntity.getStore();
        if (store == null) return;

        Player player = store.getComponent(owningEntity, Player.getComponentType());
        if (player == null) return;

        CommandBuffer<EntityStore> accessor = context.getCommandBuffer();

        World world = accessor.getExternalData().getWorld();
        if (world == null) return;

        BlockPosition blockPosition = context.getTargetBlock();
        if (blockPosition == null) return;

        Vector3i blockTargetPos = new Vector3i(blockPosition.x, blockPosition.y, blockPosition.z);
        List<Vector3i> selectedPositions = this.searchBlocks(world, blockTargetPos);

        if (selectedPositions.isEmpty()) {
            LOGGER.atWarning().log("evoke: block must be a Hex item");
            return;
        }

        for (Vector3i blockPos : selectedPositions) {
            Vector3d blockVector = new Vector3d(blockPos.x + 0.5, blockPos.y, blockPos.z + 0.5);

            int blockRotationIndex = world.getBlockRotationIndex(blockPos.x, blockPos.y, blockPos.z);
            RotationTuple rotation = RotationTuple.get(blockRotationIndex);
            Rotation3f blockRotation = new Rotation3f(0.0F, (float) (rotation.yaw().getRadians() + Math.PI), 0.0F);

            BlockType blockType = world.getBlockType(blockPos);
            Map.Entry<String, String> hexItem = HexItemRegistery.getByBlockId(blockType.getId());

            if (hexItem == null) {
                LOGGER.atWarning().log("evoke: block must be a Hex item");
                return;
            }

            world.breakBlock(blockPos.x, blockPos.y, blockPos.z, 0);

            accessor.run(_store -> {
                NPCPlugin.get().spawnNPC(_store, hexItem.getValue(), null, blockVector, blockRotation);
            });
        }
    }

    @Nonnull
    private IntList getBlockIds() {
        IntArrayList result = new IntArrayList();

        ArrayList<String> hexItems = HexItemRegistery.getAllBlocks();
        for (String blockSetName : hexItems) {
            int blockIndex = BlockType.getAssetMap().getIndex(blockSetName);
            if (blockIndex != Integer.MIN_VALUE) {
                result.add(blockIndex);
            }
        }

        return result;
    }

    @Nonnull
    private List<Vector3i> searchBlocks(@Nonnull World world, @Nonnull Vector3i position) {
        IntList blockIds = this.getBlockIds();
        if (blockIds.isEmpty()) {
            return List.of();
        } else {
            int originX = MathUtil.floor(position.x);
            int originY = MathUtil.floor(position.y);
            int originZ = MathUtil.floor(position.z);
            int radiusSquared = this.range * this.range;
            EvokeHexCreatureInteraction.BlockSearchConsumer consumer = new EvokeHexCreatureInteraction.BlockSearchConsumer(
                    originX, originY, originZ, radiusSquared, this.maxCount
            );
            int minY = Math.max(0, originY - this.range);
            int maxY = Math.min(319, originY + this.range);

            for (int x = originX - this.range & -32; x < originX + this.range; x += 32) {
                for (int z = originZ - this.range & -32; z < originZ + this.range; z += 32) {
                    WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(x, z));
                    if (chunk != null) {
                        BlockChunk blockChunk = chunk.getBlockChunk();

                        for (int y = minY; y < maxY; y += 32) {
                            int sectionIndex = ChunkUtil.indexSection(y);
                            if (sectionIndex >= 0 && sectionIndex < 10) {
                                BlockSection section = blockChunk.getSectionAtIndex(sectionIndex);
                                if (!section.isSolidAir() && section.containsAny(blockIds)) {
                                    consumer.setSection(x, z, sectionIndex);
                                    section.find(blockIds, consumer);
                                }
                            }
                        }
                    }
                }
            }

            return consumer.getPickedPositions();
        }
    }

    private static class BlockSearchConsumer implements IntConsumer {
        private final int originX;
        private final int originY;
        private final int originZ;
        private final int radiusSquared;
        private final int maxCount;
        private final List<Vector3i> picked;
        private int seen = 0;
        private int chunkWorldX;
        private int chunkWorldZ;
        private int sectionBaseY;

        BlockSearchConsumer(int originX, int originY, int originZ, int radiusSquared, int maxCount) {
            this.originX = originX;
            this.originY = originY;
            this.originZ = originZ;
            this.radiusSquared = radiusSquared;
            this.maxCount = maxCount;
            this.picked = new ObjectArrayList<>(maxCount);
        }

        void setSection(int chunkWorldX, int chunkWorldZ, int sectionIndex) {
            this.chunkWorldX = chunkWorldX;
            this.chunkWorldZ = chunkWorldZ;
            this.sectionBaseY = sectionIndex * 32;
        }

        @Override
        public void accept(int blockIndex) {
            int localX = ChunkUtil.xFromIndex(blockIndex);
            int localY = ChunkUtil.yFromIndex(blockIndex);
            int localZ = ChunkUtil.zFromIndex(blockIndex);
            int worldX = this.chunkWorldX + localX;
            int worldY = this.sectionBaseY + localY;
            int worldZ = this.chunkWorldZ + localZ;
            int dx = worldX - this.originX;
            int dy = worldY - this.originY;
            int dz = worldZ - this.originZ;
            if (dx * dx + dy * dy + dz * dz <= this.radiusSquared) {
                if (this.picked.size() < this.maxCount) {
                    this.picked.add(new Vector3i(worldX, worldY, worldZ));
                } else {
                    int j = ThreadLocalRandom.current().nextInt(this.seen + 1);
                    if (j < this.maxCount) {
                        this.picked.set(j, new Vector3i(worldX, worldY, worldZ));
                    }
                }

                this.seen++;
            }
        }

        @Nonnull
        List<Vector3i> getPickedPositions() {
            return this.picked;
        }
    }
}
