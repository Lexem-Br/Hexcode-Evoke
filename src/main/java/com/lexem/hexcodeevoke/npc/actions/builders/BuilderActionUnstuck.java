package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionUnstuck;

import javax.annotation.Nonnull;

public class BuilderActionUnstuck extends BuilderActionBase {
   protected final DoubleHolder speed = new DoubleHolder();
   protected final DoubleHolder heightOffset = new DoubleHolder();
   protected final DoubleHolder turnRate = new DoubleHolder();
   protected final DoubleHolder startSpread = new DoubleHolder();
   protected boolean useSensorPosition = false;

   public BuilderActionUnstuck() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Move the NPC through blocks while it is stuck.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Moves the Hex Creature straight to its evoker (or to the wand target position while seeking it), ignoring block collisions. Meant to be used with the Evoke:IsStuck sensor.";
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionUnstuck(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionUnstuck readConfig(@Nonnull JsonElement data) {
      this.getDouble(data, "Speed", this.speed, 4.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Blocks per second the NPC moves through blocks while stuck", null);
      this.getDouble(data, "HeightOffset", this.heightOffset, 1.5, DoubleRangeValidator.between(-10.0, 10.0), BuilderDescriptorState.Stable, "Height added to the target position", null);
      this.getDouble(data, "TurnRate", this.turnRate, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "How fast the movement turns towards the target, per second", "0 moves in a straight line. Higher values make tighter curves");
      this.getDouble(data, "StartSpread", this.startSpread, 0.0, DoubleRangeValidator.between(0.0, 90.0), BuilderDescriptorState.Stable, "Degrees the first direction is turned to a random side, only used when TurnRate is above 0", null);
      this.getBoolean(data, "UseSensorPosition", aBoolean -> this.useSensorPosition = aBoolean, false, BuilderDescriptorState.Stable, "Moves to the position given by the sensor instead of the evoker", "The HeightOffset is not used, the sensor position is the exact target");
      return this;
   }

   public boolean getUseSensorPosition(@Nonnull BuilderSupport support) {
      return this.useSensorPosition;
   }

   public double getSpeed(@Nonnull BuilderSupport support) {
      return this.speed.get(support.getExecutionContext());
   }

   public double getHeightOffset(@Nonnull BuilderSupport support) {
      return this.heightOffset.get(support.getExecutionContext());
   }

   public double getTurnRate(@Nonnull BuilderSupport support) {
      return this.turnRate.get(support.getExecutionContext());
   }

   public double getStartSpread(@Nonnull BuilderSupport support) {
      return this.startSpread.get(support.getExecutionContext());
   }
}
