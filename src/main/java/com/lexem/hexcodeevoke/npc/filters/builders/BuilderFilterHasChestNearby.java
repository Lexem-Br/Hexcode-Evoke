package com.lexem.hexcodeevoke.npc.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.lexem.hexcodeevoke.npc.filters.FilterHasChestNearby;

import javax.annotation.Nonnull;

public class BuilderFilterHasChestNearby extends BuilderEntityFilterBase {
   protected final DoubleHolder horizontalRange = new DoubleHolder();
   protected final DoubleHolder verticalRange = new DoubleHolder();
   protected boolean checkCanStore = false;

   public BuilderFilterHasChestNearby() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Checks if there is a chest nearby.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return getShortDescription();
   }

   @Nonnull
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      return new FilterHasChestNearby(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<IEntityFilter> readConfig(@Nonnull JsonElement data) {
      this.requireDouble(data, "HorizontalRange", this.horizontalRange, DoubleRangeValidator.fromExclToIncl(0.0F, Double.MAX_VALUE), BuilderDescriptorState.Stable, "The horizontal range to search", null);
      this.getDouble(data, "VerticalRange", this.verticalRange, 3.0F, DoubleRangeValidator.fromExclToIncl(0.0F, Double.MAX_VALUE), BuilderDescriptorState.Stable, "The vertical range to search", null);
      this.getBoolean(data, "CheckCanStore", aBoolean -> this.checkCanStore = aBoolean, false, BuilderDescriptorState.Stable, "Check if the NPC can store the item", null);
      return this;
   }

   public double getHorizontalRange(@Nonnull BuilderSupport support) {
      return this.horizontalRange.get(support.getExecutionContext());
   }

   public double getVerticalRange(@Nonnull BuilderSupport support) {
      return this.verticalRange.get(support.getExecutionContext());
   }

   public boolean getCheckCanStore(@Nonnull BuilderSupport support) {
      return this.checkCanStore;
   }
}
