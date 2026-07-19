package com.lexem.hexcodeevoke.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.lexem.hexcodeevoke.components.EvokerComponent;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;

import javax.annotation.Nonnull;

public class ChangeStatWandParticlesInteraction extends ChangeStatWandParticlesBaseInteraction {
   public static final BuilderCodec<ChangeStatWandParticlesInteraction> CODEC = BuilderCodec.builder(
                   ChangeStatWandParticlesInteraction.class, ChangeStatWandParticlesInteraction::new, ChangeStatWandParticlesBaseInteraction.CODEC
      )
      .documentation("Changes the given stats.")
      .build();

   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

   public ChangeStatWandParticlesInteraction() {
   }

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
      if (commandBuffer == null) { return; }

      Ref<EntityStore> ref = context.getEntity();
      EntityStatMap entityStatMapComponent = commandBuffer.getComponent(ref, EntityStatMap.getComponentType());

      if (entityStatMapComponent != null) {
         Store<EntityStore> store = ref.getStore();
         CommandBuffer<EntityStore> accessor = context.getCommandBuffer();
         if (accessor == null) { return; }

         EvokerComponent evoker = store.getComponent(ref, EvokerComponent.getComponentType());
         if (evoker == null) { return; }

         Int2FloatMap entityStatsNew = new Int2FloatOpenHashMap();
         int index = EntityStatType.getAssetMap().getIndex(this.statModifier);
         entityStatsNew.put(index, evoker.getSelectedHexCreatures().length);

         entityStatMapComponent.processStatChanges(EntityStatMap.Predictable.SELF, entityStatsNew, ValueType.Absolute, ChangeStatBehaviour.Set);
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "ChangeStatInteraction{}" + super.toString();
   }
}
