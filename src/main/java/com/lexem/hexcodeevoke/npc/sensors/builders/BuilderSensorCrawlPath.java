package com.lexem.hexcodeevoke.npc.sensors.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.lexem.hexcodeevoke.npc.sensors.SensorCrawlPath;

import javax.annotation.Nonnull;

public class BuilderSensorCrawlPath extends BuilderSensorBase {

   public BuilderSensorCrawlPath() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Shortest path over block surfaces to the crawl goal";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Matches while there is a path over floors, walls and ceilings from the Hex Creature to the face of the block targeted by the wand (stored by Evoke:CaptureCrawlTarget). The path is planned once per goal, and again when a block along it changes. Provides the goal position";
   }

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorCrawlPath(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.provideFeature(Feature.Position);
      return this;
   }
}
