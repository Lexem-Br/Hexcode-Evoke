package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;

import javax.annotation.Nonnull;

public class OffHandContextConditionInteraction extends SimpleInteraction {
    public static final BuilderCodec<OffHandContextConditionInteraction> CODEC =
            BuilderCodec.builder(OffHandContextConditionInteraction.class, OffHandContextConditionInteraction::new,
                            SimpleInteraction.CODEC)
                    .build();

    @Nonnull
    @Override
    public WaitForDataFrom getWaitForDataFrom() {
        return WaitForDataFrom.Server;
    }

    @Override
    protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        context.getState().state = context.getHeldItemSectionId() == InventoryComponent.UTILITY_SECTION_ID
                ? InteractionState.Finished
                : InteractionState.Failed;
        super.tick0(firstRun, time, type, context, cooldownHandler);
    }
}
