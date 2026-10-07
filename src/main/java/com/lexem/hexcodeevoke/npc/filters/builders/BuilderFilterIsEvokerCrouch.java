package com.lexem.hexcodeevoke.npc.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.lexem.hexcodeevoke.npc.filters.FilterIsEvokerCrouch;

import javax.annotation.Nonnull;

public class BuilderFilterIsEvokerCrouch extends BuilderEntityFilterBase {

   public BuilderFilterIsEvokerCrouch() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Check if the evoker is crouching.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return getShortDescription();
   }

   @Nonnull
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      return new FilterIsEvokerCrouch();
   }
   @Nonnull
   @Override
   public Builder<IEntityFilter> readConfig(@Nonnull JsonElement data) {
      return this;
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

}
