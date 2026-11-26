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
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.lexem.hexcodeevoke.utils.hexcreature.HexCreatureUnlockUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class UnlockRecipeInteraction extends SimpleInstantInteraction {
    private String[] recipes = new String[0];

    public static final BuilderCodec<UnlockRecipeInteraction> CODEC = BuilderCodec
            .builder(UnlockRecipeInteraction.class, UnlockRecipeInteraction::new, SimpleInstantInteraction.CODEC)
            .documentation("Teaches a random recipe from the list that the user does not know yet.")
            .append(new KeyedCodec<>("Recipes", Codec.STRING_ARRAY),
                    (config, value) -> config.recipes = value,
                    config -> config.recipes)
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
        Player player = commandBuffer.getComponent(ref, Player.getComponentType());
        if (playerRef == null || player == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        List<String> candidates = getUnknownRecipes(player.getPlayerConfigData().getKnownRecipes());
        if (candidates.isEmpty()) {
            context.getState().state = InteractionState.Failed;
            NotificationUtil.sendNotification(
                    playerRef.getPacketHandler(),
                    Message.translation("evoke.recipePage.allKnown.title"),
                    Message.translation("evoke.recipePage.allKnown.description"),
                    NotificationStyle.Warning
            );
            return;
        }

        String chosen = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        if (!HexCreatureUnlockUtils.learnRecipe(ref, commandBuffer, chosen)) {
            context.getState().state = InteractionState.Failed;
            return;
        }
        HexCreatureUnlockUtils.notifyRecipeLearned(playerRef, chosen);
    }

    @Override
    protected void simulateFirstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
    }

    @Nonnull
    private List<String> getUnknownRecipes(@Nonnull Set<String> knownRecipes) {
        List<String> unknown = new ArrayList<>();
        if (recipes == null) return unknown;

        for (String itemId : recipes) {
            if (itemId == null || knownRecipes.contains(itemId) || unknown.contains(itemId)) continue;
            if (Item.getAssetMap().getAsset(itemId) == null || !HexCreatureUnlockUtils.requiresRecipeKnowledge(itemId)) continue;

            unknown.add(itemId);
        }
        return unknown;
    }
}
