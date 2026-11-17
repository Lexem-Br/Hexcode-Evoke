package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;

import javax.annotation.Nonnull;

public abstract class EvokeSimpleInteraction extends SimpleInteraction {
    protected final void complete(
            boolean success,
            boolean firstRun,
            float time,
            @Nonnull InteractionType type,
            @Nonnull InteractionContext context,
            @Nonnull CooldownHandler cooldownHandler
    ) {
        context.getState().state = success ? InteractionState.Finished : InteractionState.Failed;
        super.tick0(firstRun, time, type, context, cooldownHandler);
    }

    protected final void finish(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        complete(true, firstRun, time, type, context, cooldownHandler);
    }

    protected final void fail(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        complete(false, firstRun, time, type, context, cooldownHandler);
    }
}
