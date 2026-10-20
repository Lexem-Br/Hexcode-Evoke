package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionRefillWateringCan;

import javax.annotation.Nonnull;

public class BuilderActionRefillWateringCan extends BuilderActionBase {

   public BuilderActionRefillWateringCan() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Fill the held watering can with the water found by the sensor.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionRefillWateringCan(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionRefillWateringCan readConfig(@Nonnull JsonElement data) {
      return this;
   }
}
