package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
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
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.Map;

public class EvokeHexCreatureInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final BuilderCodec<EvokeHexCreatureInteraction> CODEC =
            BuilderCodec.builder(EvokeHexCreatureInteraction.class, EvokeHexCreatureInteraction::new,
                            SimpleInteraction.CODEC)
                    .build();

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

        Vector3i blockPos = new Vector3i(blockPosition.x, blockPosition.y, blockPosition.z);
        BlockType blockType = world.getBlockType(blockPos);
        Map.Entry<String, String> hexItem = HexItemRegistery.getByBlockId(blockType.getId());

        if (blockPos == null) {
            LOGGER.atWarning().log("evoke: block target position is null");
            return;
        } else if (hexItem == null) {
            LOGGER.atWarning().log("evoke: block must be a Hex item");
            return;
        }

        Vector3d blockVector = new Vector3d(blockPos.x + 0.5, blockPos.y, blockPos.z + 0.5);

        int blockRotationIndex = world.getBlockRotationIndex(blockPos.x, blockPos.y, blockPos.z);
        RotationTuple rotation = RotationTuple.get(blockRotationIndex);
        Rotation3f blockRotation = new Rotation3f(0.0F, (float) (rotation.yaw().getRadians() + Math.PI), 0.0F);

        world.breakBlock(blockPos.x, blockPos.y, blockPos.z, 0);

        accessor.run(_store -> {
            NPCPlugin.get().spawnNPC(_store, hexItem.getValue(), null, blockVector, blockRotation);
        });
    }
}
