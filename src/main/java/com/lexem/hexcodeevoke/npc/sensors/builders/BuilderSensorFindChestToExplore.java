package com.lexem.hexcodeevoke.npc.sensors.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.lexem.hexcodeevoke.npc.sensors.SensorFindChestToExplore;

import javax.annotation.Nonnull;

public class BuilderSensorFindChestToExplore extends BuilderSensorBase {
   protected final DoubleHolder horizontalRange = new DoubleHolder();
   protected final DoubleHolder verticalRange = new DoubleHolder();
   protected final DoubleHolder revisitDelay = new DoubleHolder();
   protected final DoubleHolder range = new DoubleHolder();
   protected final DoubleHolder minRange = new DoubleHolder();
   protected final DoubleHolder heightOffset = new DoubleHolder();

   public BuilderSensorFindChestToExplore() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Choose the next chest the Hex Creature visits to memorize its items.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Keeps the chest being explored until it is memorized (or for 20 seconds); without one, every 2 seconds picks the nearest chest around the place the exploration started that was not visited in the last RevisitDelay seconds, including remembered chests that may no longer exist. Provides the position above that chest, lowered while there is no free space, when its distance is between MinRange and Range. Use Evoke:MemorizeChest with Target Explore to read it.";
   }

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorFindChestToExplore(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.getDouble(data, "HorizontalRange", this.horizontalRange, 16.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "The horizontal range to search for chests", null);
      this.getDouble(data, "VerticalRange", this.verticalRange, 4.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "The vertical range to search for chests", null);
      this.getDouble(data, "RevisitDelay", this.revisitDelay, 120.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Seconds before a visited chest can be visited again", null);
      this.getDouble(data, "Range", this.range, 100.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum range from the position", null);
      this.getDouble(data, "MinRange", this.minRange, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Minimum range from the position", null);
      this.getDouble(data, "HeightOffset", this.heightOffset, 1.0, DoubleRangeValidator.between(0.0, 10.0), BuilderDescriptorState.Stable, "Height above the top of the chest", null);
      this.provideFeature(Feature.Position);
      return this;
   }

   public double getRange(@Nonnull BuilderSupport support) {
      return this.range.get(support.getExecutionContext());
   }

   public double getMinRange(@Nonnull BuilderSupport support) {
      return this.minRange.get(support.getExecutionContext());
   }

   public double getHeightOffset(@Nonnull BuilderSupport support) {
      return this.heightOffset.get(support.getExecutionContext());
   }

   public double getHorizontalRange(@Nonnull BuilderSupport support) {
      return this.horizontalRange.get(support.getExecutionContext());
   }

   public double getVerticalRange(@Nonnull BuilderSupport support) {
      return this.verticalRange.get(support.getExecutionContext());
   }

   public double getRevisitDelay(@Nonnull BuilderSupport support) {
      return this.revisitDelay.get(support.getExecutionContext());
   }
}
