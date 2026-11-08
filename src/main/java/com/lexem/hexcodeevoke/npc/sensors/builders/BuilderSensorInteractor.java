package com.lexem.hexcodeevoke.npc.sensors.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.ComponentContext;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorWithEntityFilters;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.lexem.hexcodeevoke.npc.sensors.SensorInteractor;

import javax.annotation.Nonnull;

public class BuilderSensorInteractor extends BuilderSensorWithEntityFilters {

   public BuilderSensorInteractor() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Test if the player of the current interaction matches the entity filters";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Unlike the Target sensor, which checks the locked target, this checks the player being handled in the InteractionInstruction.";
   }

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorInteractor(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      var builderHelper = createFilterValidationHelper(ComponentContext.SensorTarget);
      getArray(data, "Filters", filters, null, BuilderDescriptorState.Stable, "A series of entity filter sensors to test", null, builderHelper);
      return this;
   }
}
