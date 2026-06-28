package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
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
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.lexem.hexcodeevoke.hexitems.HexItemRegistery;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EvokeHexCreatureInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final BuilderCodec<EvokeHexCreatureInteraction> CODEC =
            BuilderCodec.builder(EvokeHexCreatureInteraction.class, EvokeHexCreatureInteraction::new,
                            SimpleInteraction.CODEC)
                    .build();

    protected int radius = 2;
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

        Vector3d center = new Vector3d(blockPosition.x, blockPosition.y, blockPosition.z);
        List<Vector3i> selectedPositions = gatherBlocks(center, radius, accessor);
        LOGGER.atWarning().log("selectedPositions: %s", selectedPositions);

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

            if (blockType == null) {
                LOGGER.atWarning().log("evoke: invalid block");
                return;
            }

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

    private List<Vector3i> gatherBlocks(Vector3d center, double radius, CommandBuffer<EntityStore> accessor) {
        World world = accessor.getExternalData().getWorld();
        List<Vector3i> gathered = new ArrayList<>();
        int r = (int) Math.ceil(radius);
        double radiusSq = radius * radius;
        int picked = 0;

        int cx = (int) Math.floor(center.x);
        int cy = (int) Math.floor(center.y);
        int cz = (int) Math.floor(center.z);

        IntList hexItensIds = getBlockIds();

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radiusSq) continue;
                    if (picked >= maxCount) continue;

                    int bx = cx + dx;
                    int by = cy + dy;
                    int bz = cz + dz;

                    int blockId = world.getBlock(bx, by, bz);
                    if (blockId == BlockType.EMPTY_ID) continue;
                    if(!hexItensIds.contains(blockId)) continue;

                    gathered.add(new Vector3i(bx, by, bz));
                    picked++;
                }
            }
        }

        return gathered;
    }
}
