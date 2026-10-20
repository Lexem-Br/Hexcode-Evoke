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
import com.lexem.hexcodeevoke.npc.sensors.SensorEvokerPosition;

import javax.annotation.Nonnull;

public class BuilderSensorEvokerPosition extends BuilderSensorBase {
   protected final DoubleHolder range = new DoubleHolder();
   protected final DoubleHolder minRange = new DoubleHolder();
   protected final DoubleHolder heightOffset = new DoubleHolder();
   protected final DoubleHolder leadDistance = new DoubleHolder();
   protected final DoubleHolder leadFullSpeed = new DoubleHolder();
   protected final DoubleHolder leadSideOffset = new DoubleHolder();

   public BuilderSensorEvokerPosition() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Provides the evoker's position raised by a height offset";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Matches when the evoker is within range and provides their position raised by HeightOffset, used by flying creatures to follow the player in 3D";
   }

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorEvokerPosition(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.getDouble(data, "MinRange", this.minRange, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Minimum range to the target position", null);
      this.requireDouble(data, "Range", this.range, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum range to the target position", null);
      this.getDouble(data, "HeightOffset", this.heightOffset, 1.5, DoubleRangeValidator.between(-10.0, 10.0), BuilderDescriptorState.Stable, "Height added to the evoker's position", null);
      this.getDouble(data, "LeadDistance", this.leadDistance, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "How far ahead of the evoker the position moves while they walk", "Maximum distance the position moves ahead of the evoker in their walking direction. 0 disables it");
      this.getDouble(data, "LeadFullSpeed", this.leadFullSpeed, 5.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Evoker speed at which the full lead distance is used", "Evoker horizontal speed (blocks per second) at which the full lead distance is used. Slower movement uses a proportional lead");
      this.getDouble(data, "LeadSideOffset", this.leadSideOffset, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "How far to the side of the walking direction the position moves", "Distance the position moves to the left or right of the evoker's walking direction, towards the side the creature is already on, so it doesn't block a first person view. Scales with speed like LeadDistance");
      this.provideFeature(Feature.Position);
      return this;
   }

   public double getLeadSideOffset(@Nonnull BuilderSupport support) {
      return this.leadSideOffset.get(support.getExecutionContext());
   }

   public double getLeadDistance(@Nonnull BuilderSupport support) {
      return this.leadDistance.get(support.getExecutionContext());
   }

   public double getLeadFullSpeed(@Nonnull BuilderSupport support) {
      return this.leadFullSpeed.get(support.getExecutionContext());
   }

   public double getMinRange(@Nonnull BuilderSupport support) {
      return this.minRange.get(support.getExecutionContext());
   }

   public double getRange(@Nonnull BuilderSupport support) {
      return this.range.get(support.getExecutionContext());
   }

   public double getHeightOffset(@Nonnull BuilderSupport support) {
      return this.heightOffset.get(support.getExecutionContext());
   }
}
