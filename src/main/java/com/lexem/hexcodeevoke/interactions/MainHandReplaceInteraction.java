package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class MainHandReplaceInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<MainHandReplaceInteraction> CODEC = BuilderCodec.builder(
                    MainHandReplaceInteraction.class, MainHandReplaceInteraction::new, SimpleInstantInteraction.CODEC
            )
            .documentation("Runs the interaction defined by the interaction variable of the item in the main hand.")
            .append(new KeyedCodec<>("Var", Codec.STRING),
                    (config, value) -> config.variable = value,
                    config -> config.variable)
            .addValidator(Validators.nonNull())
            .add()
            .append(new KeyedCodec<>("DefaultValue", RootInteraction.CHILD_ASSET_CODEC),
                    (config, value) -> config.defaultValue = value,
                    config -> config.defaultValue)
            .addValidatorLate(() -> RootInteraction.VALIDATOR_CACHE.getValidator().late())
            .add()
            .build();

    protected String variable;
    @Nullable
    protected String defaultValue;

    public MainHandReplaceInteraction() {
    }

    @Override
    protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        String next = resolveFromMainHand(context);
        if (next == null) next = defaultValue;

        RootInteraction nextInteraction = next != null ? RootInteraction.getAssetMap().getAsset(next) : null;
        if (nextInteraction == null) {
            context.getState().state = InteractionState.Failed;
            return;
        }

        context.getState().state = InteractionState.Finished;
        context.execute(nextInteraction);
    }

    @Nullable
    private String resolveFromMainHand(@Nonnull InteractionContext context) {
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        if (commandBuffer == null) return null;

        ItemStack mainHandItem = InventoryComponent.getItemInHand(commandBuffer, context.getEntity());
        if (mainHandItem == null) return null;

        Item item = mainHandItem.getItem();

        Map<String, String> vars = item.getInteractionVars();
        return vars != null ? vars.get(variable) : null;
    }

    @Nonnull
    @Override
    public String toString() {
        return "MainHandReplaceInteraction{variable=" + variable + "}" + super.toString();
    }
}
