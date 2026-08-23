package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.NearestBlockUtil;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.HarvestingDropType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.farming.FarmingData;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import com.lexem.hexcodeevoke.utils.FinderUtils;
import com.lexem.hexcodeevoke.utils.HexCreatureUtils;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;

public class EvokeHexCreatureInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final BuilderCodec<EvokeHexCreatureInteraction> CODEC =
            BuilderCodec.builder(EvokeHexCreatureInteraction.class, EvokeHexCreatureInteraction::new,
                            SimpleInteraction.CODEC)
                    .build();

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

            Ref<EntityStore> refESPlayer = context.getOwningEntity();
            if (refESPlayer == null) {
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            World world = accessor.getExternalData().getWorld();
            TransformComponent transformComponent = accessor.getComponent(refESPlayer, TransformComponent.getComponentType());
            if (transformComponent == null) return;

            Vector3i hexItemPosition = FinderUtils.findNearestBlockBFS(
                    transformComponent.getPosition(),
                    (int) 2.0,
                    (int) 2.0,
                    hexItemValidator,
                    world
            );

            if (hexItemPosition == null) {
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            boolean spawned = HexCreatureUtils.trySpawnHexCreature(hexItemPosition, refESPlayer, accessor);
            if (!spawned) {
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
            }

            context.getState().state = InteractionState.Finished;
            super.tick0(firstRun, time, type, context, cooldownHandler);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode evoke] HexCreature failed: %s", e.getMessage());
            context.getState().state = InteractionState.Failed;
        }
    }

    private final FinderUtils.BlockValidator<World> hexItemValidator = (block, world) -> {
        BlockType blockType = world.getBlockType(block);
        if (blockType == null || blockType.getId() == null) return false;
        AllowedHexItemsAsset.HexItem hexItem = AllowedHexItemsAsset.getByBlockId(blockType.getId());
        return hexItem != null;
    };
}
