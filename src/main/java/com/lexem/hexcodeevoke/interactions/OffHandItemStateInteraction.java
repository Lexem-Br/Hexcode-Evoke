package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.utils.OffHandItemStateUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class OffHandItemStateInteraction extends SimpleInstantInteraction {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final BuilderCodec<OffHandItemStateInteraction> CODEC =
            BuilderCodec.builder(OffHandItemStateInteraction.class, OffHandItemStateInteraction::new,
                            SimpleInstantInteraction.CODEC)
                    .append(new KeyedCodec<>("Items", new ArrayCodec<>(Codec.STRING, String[]::new)),
                            (config, o) -> config.items = o,
                            config -> config.items
                    )
                    .add()
                    .append(new KeyedCodec<>("State", Codec.STRING),
                            (config, value) -> config.state = value,
                            config -> config.state)
                    .add()
                    .afterDecode(config -> OffHandItemStateUtils.registerTransientItems(config.items))
                    .build();
    protected String[] items;
    @Nullable
    protected String state;

    @Override
    protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        try {
            CommandBuffer<EntityStore> accessor = context.getCommandBuffer();
            if (accessor == null || items == null) return;

            ItemStack newItemStack = OffHandItemStateUtils.setState(context.getEntity(), accessor, Arrays.asList(items), state);
            if (newItemStack != null && context.getHeldItemSectionId() == InventoryComponent.UTILITY_SECTION_ID) {
                context.setHeldItem(newItemStack);
            }
        } catch (Exception e) {
            LOGGER.atSevere().log("[hexcode evoke] OffHandItemStateInteraction failed: %s", e.getMessage());
        }
    }
}
