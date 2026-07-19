package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.components.EvokerComponent;

import javax.annotation.Nonnull;

public class EvokeSelectAllHCInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final BuilderCodec<EvokeSelectAllHCInteraction> CODEC =
            BuilderCodec.builder(EvokeSelectAllHCInteraction.class, EvokeSelectAllHCInteraction::new,
                            SimpleInteraction.CODEC)
                    .build();

    @Override
    protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        try {
            Ref<EntityStore> playerRef = context.getOwningEntity();
            if (playerRef == null) {
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            Store<EntityStore> store = playerRef.getStore();
            CommandBuffer<EntityStore> accessor = context.getCommandBuffer();
            if (accessor == null) {
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            EvokerComponent evoker = store.getComponent(playerRef, EvokerComponent.getComponentType());
            if (evoker == null) {
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            evoker.selectAllHexCreatures();

            context.getState().state = InteractionState.Finished;
            super.tick0(firstRun, time, type, context, cooldownHandler);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode evoke] EvokeTargetPosition failed: %s", e.getMessage());
            context.getState().state = InteractionState.Failed;
        }
    }

}
