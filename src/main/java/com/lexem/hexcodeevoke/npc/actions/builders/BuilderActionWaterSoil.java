package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionWaterSoil;

import javax.annotation.Nonnull;

public class BuilderActionWaterSoil extends BuilderActionBase {

   public BuilderActionWaterSoil() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Water the soil.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionWaterSoil(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionWaterSoil readConfig(@Nonnull JsonElement data) {
      return this;
   }
}
