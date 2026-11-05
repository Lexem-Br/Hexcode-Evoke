package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.lexem.hexcodeevoke.utils.VoidArenaManager;
import org.joml.Vector3i;

import javax.annotation.Nonnull;

public class VoidArenaActivateInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<VoidArenaActivateInteraction> CODEC = BuilderCodec
            .builder(VoidArenaActivateInteraction.class, VoidArenaActivateInteraction::new, SimpleInstantInteraction.CODEC)
            .build();

    @Nonnull
    @Override
    public WaitForDataFrom getWaitForDataFrom() {
        return WaitForDataFrom.Server;
    }

    @Override
    protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        BlockPosition targetBlock = context.getTargetBlock();
        if (commandBuffer == null || targetBlock == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        World world = commandBuffer.getExternalData().getWorld();
        VoidArenaManager.ActivationResult result = VoidArenaManager.activate(world, new Vector3i(targetBlock.x, targetBlock.y, targetBlock.z));
        if (result == VoidArenaManager.ActivationResult.STARTED) return;

        context.getState().state = InteractionState.Failed;
        PlayerRef playerRef = commandBuffer.getComponent(context.getEntity(), PlayerRef.getComponentType());
        if (playerRef == null) return;

        Message description = result == VoidArenaManager.ActivationResult.FIGHTING
                ? VoidArenaManager.getWaveMessage(world)
                : Message.translation("evoke.voidArena.altar.spent");
        NotificationUtil.sendNotification(playerRef.getPacketHandler(), Message.translation("evoke.voidArena.altar.title"), description, NotificationStyle.Default);
    }

    @Override
    protected void simulateFirstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
    }
}
