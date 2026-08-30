package com.lexem.hexcodeevoke.npc.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.IntHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.IntSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.lexem.hexcodeevoke.npc.filters.FilterNeedsMoreMinions;

import javax.annotation.Nonnull;

public class BuilderFilterNeedsMoreMinions extends BuilderEntityFilterBase {
   protected final IntHolder desiredQuantity = new IntHolder();

   public BuilderFilterNeedsMoreMinions() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Checks if more minions should be added.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return getShortDescription();
   }

   @Nonnull
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      return new FilterNeedsMoreMinions(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<IEntityFilter> readConfig(@Nonnull JsonElement data) {
      this.requireInt(data, "DesiredQuantity", this.desiredQuantity, IntSingleValidator.greater0(), BuilderDescriptorState.Stable, "Desired number of minions", null);
      return this;
   }

   public int getHorizontalRange(@Nonnull BuilderSupport support) {
      return this.desiredQuantity.get(support.getExecutionContext());
   }

   public int getVerticalRange(@Nonnull BuilderSupport support) {
      return this.desiredQuantity.get(support.getExecutionContext());
   }
}
