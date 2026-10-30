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
import com.lexem.hexcodeevoke.components.HexCreatureComponent;
import com.lexem.hexcodeevoke.npc.sensors.SensorSearchedChestPosition;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class BuilderSensorSearchedChestPosition extends BuilderSensorBase {
   protected final DoubleHolder range = new DoubleHolder();
   protected final DoubleHolder minRange = new DoubleHolder();
   protected final DoubleHolder heightOffset = new DoubleHolder();
   protected ChestTarget target;

   public BuilderSensorSearchedChestPosition() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Position above the chest that has the item searched in the Arcane Codex";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Position above the chest chosen in the search page (or the chest being explored), lowered while there is no free space for the NPC";
   }

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorSearchedChestPosition(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.getDouble(data, "MinRange", this.minRange, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Minimum range from the position", null);
      this.requireDouble(data, "Range", this.range, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum range from the position", null);
      this.getDouble(data, "HeightOffset", this.heightOffset, 1.0, DoubleRangeValidator.between(0.0, 10.0), BuilderDescriptorState.Stable, "Height above the top of the chest", null);
      this.getEnum(data, "Target", v -> this.target = v, ChestTarget.class, ChestTarget.Search, BuilderDescriptorState.Stable, "Which chest the position is above", null);
      this.provideFeature(Feature.Position);
      return this;
   }

   public ChestTarget getTarget() {
      return this.target;
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

   public enum ChestTarget implements Supplier<String> {
      Search("The chest chosen in the search page"),
      Explore("The chest being explored");

      private final String description;

      ChestTarget(String description) {
         this.description = description;
      }

      @Override
      public String get() {
         return this.description;
      }

      @Nullable
      public Vector3d getPosition(@Nonnull HexCreatureComponent hexCreature) {
         return this == Explore ? hexCreature.getExploreChestPosition() : hexCreature.getSearchChestPosition();
      }
   }
}
