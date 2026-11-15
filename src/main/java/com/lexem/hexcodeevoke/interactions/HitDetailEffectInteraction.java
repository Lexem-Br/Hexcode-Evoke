package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class HitDetailEffectInteraction extends SimpleInstantInteraction {
    private Map<String, String> effects = Map.of();

    public static final BuilderCodec<HitDetailEffectInteraction> CODEC = BuilderCodec
            .builder(HitDetailEffectInteraction.class, HitDetailEffectInteraction::new, SimpleInstantInteraction.CODEC)
            .documentation("Applies an entity effect to the target according to the model detail box that a projectile hit, so a model region can work as a weak point.")
            .append(new KeyedCodec<Map<String, String>>("Effects", new MapCodec<>(Codec.STRING, HashMap::new)),
                    (o, v) -> o.effects = v,
                    o -> o.effects)
            .documentation("Entity effect applied to the target for each detail box name. Hits on other detail boxes, or without one, apply nothing.")
            .add()
            .build();

    @Override
    protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
        String detail = context.getMetaStore().getIfPresentMetaObject(Interaction.HIT_DETAIL);
        if (detail == null) return;
        String effectId = effects.get(detail);
        if (effectId == null) return;

        EntityEffect effect = EntityEffect.getAssetMap().getAsset(effectId);
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        Ref<EntityStore> target = context.getTargetEntity();
        if (effect == null || commandBuffer == null || target == null || !target.isValid()) return;

        EffectControllerComponent controller = commandBuffer.getComponent(target, EffectControllerComponent.getComponentType());
        if (controller == null) return;

        Ref<EntityStore> owner = context.getOwningEntity();
        controller.addEffect(target, effect, commandBuffer, owner != null && owner.isValid() && !owner.equals(target) ? owner : null);
    }

    @Override
    protected void simulateFirstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
    }
}
