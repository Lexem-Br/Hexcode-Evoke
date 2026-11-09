package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.lexem.hexcodeevoke.utils.VoidArenaManager;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OpenVoidArenaPortalInteraction extends SimpleInstantInteraction {
    private String portalBlock = VoidArenaManager.PORTAL_BLOCK;
    private int durationSeconds = 300;
    private double distance = 3.0;

    public static final BuilderCodec<OpenVoidArenaPortalInteraction> CODEC = BuilderCodec
            .builder(OpenVoidArenaPortalInteraction.class, OpenVoidArenaPortalInteraction::new, SimpleInstantInteraction.CODEC)
            .append(new KeyedCodec<>("PortalBlock", Codec.STRING),
                    (config, value) -> config.portalBlock = value,
                    config -> config.portalBlock)
            .add()
            .append(new KeyedCodec<>("DurationSeconds", Codec.INTEGER),
                    (config, value) -> config.durationSeconds = value,
                    config -> config.durationSeconds)
            .add()
            .append(new KeyedCodec<>("Distance", Codec.DOUBLE),
                    (config, value) -> config.distance = value,
                    config -> config.distance)
            .add()
            .build();

    @Nonnull
    @Override
    public WaitForDataFrom getWaitForDataFrom() {
        return WaitForDataFrom.Server;
    }

    @Override
    protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        if (commandBuffer == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        Ref<EntityStore> ref = context.getEntity();
        World world = commandBuffer.getExternalData().getWorld();
        PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
        if (VoidArenaManager.isArenaWorld(world)) {
            fail(context, playerRef, "evoke.voidArena.key.insideArena");
            return;
        }

        TransformComponent transform = commandBuffer.getComponent(ref, TransformComponent.getComponentType());
        if (transform == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        HeadRotation headRotation = commandBuffer.getComponent(ref, HeadRotation.getComponentType());
        float yaw = headRotation != null ? headRotation.getRotation().yaw() : transform.getRotation().yaw();
        Vector3i pos = VoidArenaManager.openPortal(world, transform.getPosition(), yaw, distance, portalBlock, durationSeconds, commandBuffer);
        if (pos == null) {
            fail(context, playerRef, "evoke.voidArena.key.noRoom");
            return;
        }

        if (playerRef != null) {
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(),
                    Message.translation("evoke.voidArena.key.opened.title"),
                    Message.translation("evoke.voidArena.key.opened.description").param("minutes", Math.max(1, durationSeconds / 60)),
                    NotificationStyle.Success
            );
        }
    }

    @Override
    protected void simulateFirstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
    }

    private static void fail(@Nonnull InteractionContext context, @Nullable PlayerRef playerRef, @Nonnull String description) {
        context.getState().state = InteractionState.Failed;
        if (playerRef == null) return;

        NotificationUtil.sendNotification(
                playerRef.getPacketHandler(),
                Message.translation("evoke.voidArena.key.failed"),
                Message.translation(description),
                NotificationStyle.Warning
        );
    }
}
