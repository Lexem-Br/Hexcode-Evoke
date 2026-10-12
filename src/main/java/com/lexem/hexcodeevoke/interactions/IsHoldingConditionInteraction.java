package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class IsHoldingConditionInteraction extends SimpleInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final BuilderCodec<IsHoldingConditionInteraction> CODEC =
            BuilderCodec.builder(IsHoldingConditionInteraction.class, IsHoldingConditionInteraction::new,
                            SimpleInteraction.CODEC)
                    .append(new KeyedCodec<>("MainHanded", Codec.BOOLEAN),
                            (config, value) -> config.mainHanded = value,
                            (config) -> config.mainHanded)
                    .add()
                    .append(new KeyedCodec<>("Items", new ArrayCodec<>(Codec.STRING, String[]::new)),
                            (config, o) -> config.items = o,
                            config -> config.items
                    )
                    .add()
                    .build();
    private boolean mainHanded = true;
    protected String[] items;

    @Override
    protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        try {
            CommandBuffer<EntityStore> accessor = context.getCommandBuffer();
            if (accessor == null) {
                context.getState().state = InteractionState.Failed;
                super.tick0(firstRun, time, type, context, cooldownHandler);
                return;
            }

            if (mainHanded) {
                final ItemStack itemInHand = InventoryComponent.getItemInHand(accessor, context.getEntity());

                for (String item : items) {
                    if (itemInHand != null && item.equals(itemInHand.getItemId())) {
                        context.getState().state = InteractionState.Finished;
                        super.tick0(firstRun, time, type, context, cooldownHandler);
                        return;
                    }
                }
            } else {
                final var utilityComponent = accessor.getComponent(context.getEntity(), InventoryComponent.Utility.getComponentType());
                final var itemInOffHand = utilityComponent != null ? utilityComponent.getActiveItem() : null;
                for (String item : items) {
                    if (itemInOffHand != null && item.equals(itemInOffHand.getItemId())) {
                        context.getState().state = InteractionState.Finished;
                        super.tick0(firstRun, time, type, context, cooldownHandler);
                        return;
                    }
                }
            }

            context.getState().state = InteractionState.Failed;
            super.tick0(firstRun, time, type, context, cooldownHandler);
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode evoke] IsHoldingConditionInteraction failed: %s", e.getMessage());
            context.getState().state = InteractionState.Failed;
        }
    }
}
