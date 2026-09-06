package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionCreateTasks;

import javax.annotation.Nonnull;

public class BuilderActionCreateTasks extends BuilderActionBase {
   protected final DoubleHolder horizontalRange = new DoubleHolder();
   protected final DoubleHolder verticalRange = new DoubleHolder();

   public BuilderActionCreateTasks() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Create tasks to store items.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionCreateTasks(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionCreateTasks readConfig(@Nonnull JsonElement data) {
      this.requireDouble(data, "HorizontalRange", this.horizontalRange, DoubleRangeValidator.fromExclToIncl(0.0F, Double.MAX_VALUE), BuilderDescriptorState.Stable, "The horizontal range to search", null);
      this.getDouble(data, "VerticalRange", this.verticalRange, 3.0F, DoubleRangeValidator.fromExclToIncl(0.0F, Double.MAX_VALUE), BuilderDescriptorState.Stable, "The vertical range to search", null);
      return this;
   }

   public double getHorizontalRange(@Nonnull BuilderSupport support) {
      return this.horizontalRange.get(support.getExecutionContext());
   }

   public double getVerticalRange(@Nonnull BuilderSupport support) {
      return this.verticalRange.get(support.getExecutionContext());
   }

}
