package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import com.lexem.hexcodeevoke.utils.block.BlockUtils;
import com.lexem.hexcodeevoke.utils.block.FinderUtils;
import com.lexem.hexcodeevoke.utils.common.ErrorMessageUtils;
import com.lexem.hexcodeevoke.utils.hexcreature.HexCreatureReverter;
import com.lexem.hexcodeevoke.utils.hexcreature.HexCreatureUtils;
import org.joml.Vector3i;

import javax.annotation.Nonnull;

public class HexCreatureInteraction extends EvokeSimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final BuilderCodec<HexCreatureInteraction> CODEC =
            BuilderCodec.builder(HexCreatureInteraction.class, HexCreatureInteraction::new,
                            SimpleInteraction.CODEC)
                    .build();

    @Override
    protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        try {
            CommandBuffer<EntityStore> accessor = context.getCommandBuffer();
            if (accessor == null) {
                fail(firstRun, time, type, context, cooldownHandler);
                return;
            }

            Ref<EntityStore> refESPlayer = context.getOwningEntity();
            if (refESPlayer == null) {
                fail(firstRun, time, type, context, cooldownHandler);
                return;
            }

            Ref<EntityStore> targetEntity = TargetUtil.getTargetEntity(refESPlayer, accessor);
            if (HexCreatureReverter.getHexCreature(targetEntity, accessor) != null) {
                boolean reverted = HexCreatureReverter.tryRevert(refESPlayer, targetEntity, accessor);
                complete(reverted, firstRun, time, type, context, cooldownHandler);
                return;
            }

            BlockPosition blockPosition = context.getTargetBlock();
            if (blockPosition == null) {
                fail(firstRun, time, type, context, cooldownHandler);
                return;
            }

            World world = accessor.getExternalData().getWorld();
            TransformComponent transformComponent = accessor.getComponent(refESPlayer, TransformComponent.getComponentType());
            if (transformComponent == null) return;

            Vector3i hexItemPosition = FinderUtils.findNearestBlock(
                    transformComponent.getPosition(),
                    3,
                    3,
                    hexItemValidator,
                    world
            );

            if (hexItemPosition == null) {
                fail(firstRun, time, type, context, cooldownHandler);
                return;
            }

            String blockId = BlockUtils.getBlockIdByPosition(hexItemPosition, world);
            AllowedHexItemsAsset.HexItem hexItem = AllowedHexItemsAsset.getByBlockId(blockId);
            if (hexItem == null) {
                PlayerRef playerRef = accessor.getComponent(refESPlayer, PlayerRef.getComponentType());
                ErrorMessageUtils.sendMessageError(playerRef, "errors.invalid_target", "errors.block_must_be_hex_item");
                fail(firstRun, time, type, context, cooldownHandler);
                return;
            }

            boolean spawned = HexCreatureUtils.trySpawnHexCreature(hexItemPosition, refESPlayer, accessor);
            if (!spawned) {
                fail(firstRun, time, type, context, cooldownHandler);
                return;
            }

            finish(firstRun, time, type, context, cooldownHandler);
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log("[hexcode evoke] HexCreature failed");
            context.getState().state = InteractionState.Failed;
        }
    }

    private final FinderUtils.BlockValidator<World> hexItemValidator = (block, world) -> {
        String blockId = BlockUtils.getBlockIdByPosition(block, world);
        if (blockId == null) return false;

        AllowedHexItemsAsset.HexItem hexItem = AllowedHexItemsAsset.getByBlockId(blockId);
        return hexItem != null;
    };

}
