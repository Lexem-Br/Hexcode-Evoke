package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.util.InteractionTarget;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;

import javax.annotation.Nullable;

public abstract class ChangeStatWandParticlesBaseInteraction extends SimpleInstantInteraction {
   public static final BuilderCodec<ChangeStatWandParticlesBaseInteraction> CODEC = BuilderCodec.abstractBuilder(
         ChangeStatWandParticlesBaseInteraction.class, SimpleInstantInteraction.CODEC
      )
      .append(new KeyedCodec<>("StatModifier", Codec.STRING),
             (config, value, info) -> config.statModifier = value,
             (config, info) -> config.statModifier)
      .documentation("Modifiers to apply to EntityStats.")
      .add()
      .<InteractionTarget>appendInherited(
         new KeyedCodec<>("Entity", InteractionTarget.CODEC), (o, i) -> o.entityTarget = i, o -> o.entityTarget, (o, p) -> o.entityTarget = p.entityTarget
      )
      .documentation("The entity to target for this interaction.")
      .addValidator(Validators.nonNull())
      .add()
      .build();
   protected Object2FloatMap<String> entityStatAssets;
   @Nullable
   protected String statModifier;
   protected InteractionTarget entityTarget = InteractionTarget.USER;

   public ChangeStatWandParticlesBaseInteraction() {
   }


}
