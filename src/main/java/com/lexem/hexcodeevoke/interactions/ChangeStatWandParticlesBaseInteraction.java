package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;

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
      .build();

   @Nullable
   protected String statModifier;

   public ChangeStatWandParticlesBaseInteraction() {
   }
}
