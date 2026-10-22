package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionResetChestExploration;

import javax.annotation.Nonnull;

public class BuilderActionResetChestExploration extends BuilderActionBase {
   protected boolean clearVisited = true;

   public BuilderActionResetChestExploration() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Start a new chest exploration around the current position.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "The chests explored with Evoke:FindChestToExplore are the ones around the current position, and every chest can be visited again unless ClearVisited is false.";
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionResetChestExploration(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionResetChestExploration readConfig(@Nonnull JsonElement data) {
      this.getBoolean(data, "ClearVisited", aBoolean -> this.clearVisited = aBoolean, true, BuilderDescriptorState.Stable, "Forget which chests were already visited", null);
      return this;
   }

   public boolean getClearVisited() {
      return this.clearVisited;
   }
}
