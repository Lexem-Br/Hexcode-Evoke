package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.hexitems.HexItemRegistery;
import com.lexem.hexcodeevoke.utils.HexCreatureUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class EvokeHexCreatureInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final BuilderCodec<EvokeHexCreatureInteraction> CODEC =
            BuilderCodec.builder(EvokeHexCreatureInteraction.class, EvokeHexCreatureInteraction::new,
                            SimpleInteraction.CODEC)
                    .build();

    protected int radius = 2;
    protected int maxCount = 1;

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

            Vector3d center = new Vector3d(blockPosition.x, blockPosition.y, blockPosition.z);
            List<Vector3i> selectedPositions = gatherBlocks(center, radius, accessor);

            if (selectedPositions.isEmpty()) {
                LOGGER.atWarning().log("evoke: block must be a Hex item");
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            Ref<EntityStore> refESPlayer = context.getOwningEntity();
            if (refESPlayer == null) {
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
            }

            for (Vector3i blockPos : selectedPositions) {
                boolean spawned = HexCreatureUtils.trySpawnHexCreature(blockPos, refESPlayer, accessor);

                if (!spawned) {
                    context.getState().state = InteractionState.Failed;
                    super.tick0(firstRun, time, type, context, cooldownHandler);
                }
            }

            context.getState().state = InteractionState.Finished;
            super.tick0(firstRun, time, type, context, cooldownHandler);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode evoke] HexCreature failed: %s", e.getMessage());
            context.getState().state = InteractionState.Failed;
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
