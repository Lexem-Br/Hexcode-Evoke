package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;

import javax.annotation.Nonnull;

public class SendMessageInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private String title = "Error";
    private String description = "";

    public static final BuilderCodec<SendMessageInteraction> CODEC =
            BuilderCodec.builder(SendMessageInteraction.class, SendMessageInteraction::new,
                            SimpleInteraction.CODEC)
                    .append(new KeyedCodec<>("Title", Codec.STRING),
                            (config, value) -> config.title = value,
                            (config) -> config.title)
                    .add()
                    .append(new KeyedCodec<>("Description", Codec.STRING),
                            (config, value) -> config.description = value,
                            (config) -> config.description)
                    .add()
                    .build();

    @Override
    protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        try {
            Ref<EntityStore> refESPlayer = context.getOwningEntity();
            if (refESPlayer == null) return;

            Store<EntityStore> store = context.getEntity().getStore();
            PlayerRef playerRef = store.getComponent(refESPlayer, PlayerRef.getComponentType());
            if (playerRef != null) {
                NotificationUtil.sendNotification(
                        playerRef.getPacketHandler(), Message.translation(title),
                        Message.translation(description)
                );
            }

            context.getState().state = InteractionState.Finished;
            super.tick0(firstRun, time, type, context, cooldownHandler);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode evoke] SendMessageInteraction failed: %s", e.getMessage());
            context.getState().state = InteractionState.Failed;
        }
    }

}
