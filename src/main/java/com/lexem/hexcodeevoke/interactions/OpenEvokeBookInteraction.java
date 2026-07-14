package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.components.messaging.BeaconSupport;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.pages.EvokeBookPage;

import javax.annotation.Nonnull;

public class OpenEvokeBookInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final BuilderCodec<OpenEvokeBookInteraction> CODEC =
            BuilderCodec.builder(OpenEvokeBookInteraction.class, OpenEvokeBookInteraction::new,
                            SimpleInteraction.CODEC)
                    .build();

    @Override
    protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        try {
            Ref<EntityStore> refESPlayer = context.getOwningEntity();
            if (refESPlayer == null) { return; }

            Store<EntityStore> store = refESPlayer.getStore();
            PlayerRef playerRef = store.getComponent(refESPlayer, PlayerRef.getComponentType());
            if (playerRef == null) { return; }

            EvokerComponent evoker = store.getComponent(refESPlayer, EvokerComponent.getComponentType());
            if (evoker != null && evoker.getHexCreatureUUIDs() != null && evoker.getHexCreatureUUIDs().length > 0) {
                CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
                if (commandBuffer != null) {
                    World world = commandBuffer.getExternalData().getWorld();
                    evoker.deleteUnusedHexCreatureUUID(world, evoker.getHexCreatureUUIDs());
                }
            }

            EvokeBookPage evokeBookPage = new EvokeBookPage();
            evokeBookPage.mainPage(store, playerRef, refESPlayer);

            context.getState().state = InteractionState.Finished;
            super.tick0(firstRun, time, type, context, cooldownHandler);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode evoke] OpenEvokeBook failed: %s", e.getMessage());
            context.getState().state = InteractionState.Failed;
        }
    }

}
