package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionFertilizeSoil;

import javax.annotation.Nonnull;

public class BuilderActionFertilizeSoil extends BuilderActionBase {

   public BuilderActionFertilizeSoil() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Fertilize the soil and consume the fertilizer in hand.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionFertilizeSoil(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionFertilizeSoil readConfig(@Nonnull JsonElement data) {
      return this;
   }
}
