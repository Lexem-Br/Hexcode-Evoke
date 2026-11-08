package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionStartRefuel;

import javax.annotation.Nonnull;

public class BuilderActionStartRefuel extends BuilderActionBase {

   public BuilderActionStartRefuel() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Remember the Essence Tray given by the sensor, the current state and the current position.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Used with Evoke:NeedsEssenceTray right before switching to the Refuel state, so Evoke:RefuelPosition knows where the tray is and where to come back to, and Evoke:FinishRefuel restores the state";
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionStartRefuel(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionStartRefuel readConfig(@Nonnull JsonElement data) {
      this.requireFeature(Feature.AnyPosition);
      return this;
   }
}
