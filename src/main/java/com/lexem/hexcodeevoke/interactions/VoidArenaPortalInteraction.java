package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.builtin.instances.interactions.TeleportInstanceInteraction;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.lexem.hexcodeevoke.utils.voidarena.VoidArenaManager;
import com.lexem.hexcodeevoke.utils.voidarena.VoidArenaPortals;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VoidArenaPortalInteraction extends TeleportInstanceInteraction {
    private static final long NOTIFY_COOLDOWN_MILLIS = 2000L;
    private static final Map<UUID, Long> LAST_NOTIFY = new ConcurrentHashMap<>();

    private int tier = 1;

    public static final BuilderCodec<VoidArenaPortalInteraction> CODEC = BuilderCodec
            .builder(VoidArenaPortalInteraction.class, VoidArenaPortalInteraction::new, TeleportInstanceInteraction.CODEC)
            .append(new KeyedCodec<>("Tier", Codec.INTEGER),
                    (config, value) -> config.tier = value,
                    config -> config.tier)
            .addValidator(Validators.range(1, VoidArenaManager.MAX_TIER))
            .add()
            .build();

    @Override
    protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        BlockPosition targetBlock = context.getTargetBlock();
        if (commandBuffer != null && targetBlock != null) {
            Message denial = VoidArenaPortals.getPortalDenial(commandBuffer.getExternalData().getWorld(), targetBlock.x, targetBlock.y, targetBlock.z);
            if (denial != null) {
                notifyDenied(commandBuffer, context.getEntity(), denial);
                context.getState().state = InteractionState.Failed;
                return;
            }
            VoidArenaPortals.trackPortal(commandBuffer.getExternalData().getWorld(), targetBlock.x, targetBlock.y, targetBlock.z);
        }

        super.firstRun(type, context, cooldownHandler);

        if (commandBuffer != null && targetBlock != null) {
            VoidArenaPortals.assignTier(commandBuffer.getExternalData().getWorld(), targetBlock.x, targetBlock.y, targetBlock.z, tier);
        }
    }

    private static void notifyDenied(@Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Ref<EntityStore> ref, @Nonnull Message reason) {
        PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;

        long now = System.currentTimeMillis();
        Long last = LAST_NOTIFY.get(playerRef.getUuid());
        if (last != null && now - last < NOTIFY_COOLDOWN_MILLIS) return;

        LAST_NOTIFY.values().removeIf(sentAt -> now - sentAt >= NOTIFY_COOLDOWN_MILLIS);
        LAST_NOTIFY.put(playerRef.getUuid(), now);
        NotificationUtil.sendNotification(playerRef.getPacketHandler(), Message.translation("evoke.voidArena.portal.denied"), reason, NotificationStyle.Warning);
    }
}
