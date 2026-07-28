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
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.hexitems.HexItemRegistery;
import com.lexem.hexcodeevoke.pages.EvokeBookPage;
import com.lexem.hexcodeevoke.pages.records.HexCreatureRecord;
import com.lexem.hexcodeevoke.utils.HexCreatureUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OpenEvokeBookInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String DEFAULT_ICON = "Hex_Mannequin_Block";
    private final HexCreatureUtils hexCreatureUtils = new HexCreatureUtils();

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

            CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
            if (commandBuffer == null) { return; }

            World world = commandBuffer.getExternalData().getWorld();

            EvokerComponent evoker = store.getComponent(refESPlayer, EvokerComponent.getComponentType());
            if (evoker != null && evoker.getHexCreatureUUIDs() != null && evoker.getHexCreatureUUIDs().length > 0) {
                evoker.deleteUnusedHexCreatureUUID(world, evoker.getHexCreatureUUIDs());
            }

            Player player = store.getComponent(refESPlayer, Player.getComponentType());
            if (player == null) { return; }

            EvokeBookPage evokeBookPage = new EvokeBookPage(playerRef);
            player.getPageManager().openCustomPage(refESPlayer, store, evokeBookPage);

            context.getState().state = InteractionState.Finished;
            super.tick0(firstRun, time, type, context, cooldownHandler);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode evoke] OpenEvokeBook failed: %s", e.getMessage());
            context.getState().state = InteractionState.Failed;
        }
    }

}
