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
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import com.lexem.hexcodeevoke.hexitems.AllowedHexItemsAsset;
import com.lexem.hexcodeevoke.utils.hexcreature.HexCreatureUnlockUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class UnlockHexCreatureInteraction extends SimpleInstantInteraction {
    private String[] hexCreatures = new String[0];

    public static final BuilderCodec<UnlockHexCreatureInteraction> CODEC = BuilderCodec
            .builder(UnlockHexCreatureInteraction.class, UnlockHexCreatureInteraction::new, SimpleInstantInteraction.CODEC)
            .documentation("Registers a random Hex Creature from the list that the user has not registered yet and teaches the recipes of its items.")
            .append(new KeyedCodec<>("HexCreatures", Codec.STRING_ARRAY),
                    (config, value) -> config.hexCreatures = value,
                    config -> config.hexCreatures)
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
        PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
        EvokerComponent evoker = commandBuffer.getComponent(ref, EvokerComponent.getComponentType());
        if (playerRef == null || evoker == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        List<AllowedHexItemsAsset.HexItem> candidates = getLockedCreatures(evoker);
        if (candidates.isEmpty()) {
            context.getState().state = InteractionState.Failed;
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(),
                    Message.translation("evoke.page.allKnown.title"),
                    Message.translation("evoke.page.allKnown.description"),
                    NotificationStyle.Warning
            );
            return;
        }

        AllowedHexItemsAsset.HexItem chosen = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        if (!HexCreatureUnlockUtils.unlock(ref, commandBuffer, evoker, chosen)) {
            context.getState().state = InteractionState.Failed;
        }
    }

    @Override
    protected void simulateFirstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
    }

    @Nonnull
    private List<AllowedHexItemsAsset.HexItem> getLockedCreatures(@Nonnull EvokerComponent evoker) {
        List<AllowedHexItemsAsset.HexItem> locked = new ArrayList<>();
        if (hexCreatures == null) return locked;

        for (String entityId : hexCreatures) {
            AllowedHexItemsAsset.HexItem hexItem = entityId != null ? AllowedHexItemsAsset.getByEntityId(entityId) : null;
            if (hexItem == null) continue;

            AllowedHexItemsAsset.HexItem mainItem = AllowedHexItemsAsset.getMainItem(hexItem);
            if (!HexCreatureUnlockUtils.isUnlocked(evoker, mainItem) && !locked.contains(mainItem)) locked.add(mainItem);
        }
        return locked;
    }
}
