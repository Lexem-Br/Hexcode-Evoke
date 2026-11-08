package com.lexem.hexcodeevoke.npc.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.lexem.hexcodeevoke.npc.filters.FilterHasWateringCan;

import javax.annotation.Nonnull;

public class BuilderFilterHasWateringCan extends BuilderEntityFilterBase {
   protected boolean filled = true;

   public BuilderFilterHasWateringCan() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Checks if the entity is holding a watering can with or without water.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return getShortDescription();
   }

   @Nonnull
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      return new FilterHasWateringCan(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<IEntityFilter> readConfig(@Nonnull JsonElement data) {
      this.getBoolean(data, "Filled", aBoolean -> this.filled = aBoolean, true, BuilderDescriptorState.Stable, "True to check for a watering can with water, false for an empty one", null);
      return this;
   }

   public boolean getFilled(@Nonnull BuilderSupport support) {
      return this.filled;
   }
}
