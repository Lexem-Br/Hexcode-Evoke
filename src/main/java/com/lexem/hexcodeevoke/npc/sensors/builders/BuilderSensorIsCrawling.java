package com.lexem.hexcodeevoke.npc.sensors.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.lexem.hexcodeevoke.npc.sensors.SensorIsCrawling;

import javax.annotation.Nonnull;

public class BuilderSensorIsCrawling extends BuilderSensorBase {

   public BuilderSensorIsCrawling() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Test if the Hex Creature is crawling over block surfaces";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Matches while Evoke:Crawl is moving the Hex Creature or holding it on a wall or ceiling. No target is returned";
   }

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorIsCrawling(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      return this;
   }
}
