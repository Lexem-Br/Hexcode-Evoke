package com.lexem.hexcodeevoke.npc.actions.voidarena.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.voidarena.ActionVoidArenaBlink;

import javax.annotation.Nonnull;

public class BuilderActionVoidArenaBlink extends BuilderActionBase {
   private final NumberArrayHolder distanceRange = new NumberArrayHolder();
   private double maxYOffset;

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Teleport the NPC away from the nearest player.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Picks the reachable position around the NPC that best puts distance between it and the nearest player, without crossing walls.";
   }

   @Nonnull
   @Override
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionVoidArenaBlink(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public BuilderActionVoidArenaBlink readConfig(@Nonnull JsonElement data) {
      this.requireDoubleRange(data, "DistanceRange", this.distanceRange, DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, Double.MAX_VALUE), BuilderDescriptorState.Stable, "How far from its current position the NPC can land", null);
      this.getDouble(data, "MaxYOffset", value -> this.maxYOffset = value, 5.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "How far above or below its current height the NPC can land", null);
      return this;
   }

   public double[] getDistanceRange(@Nonnull BuilderSupport support) {
      return this.distanceRange.get(support.getExecutionContext());
   }

   public double getMaxYOffset() {
      return this.maxYOffset;
   }
}
