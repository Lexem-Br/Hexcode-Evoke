package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionTakeItemFromChestTask;

import javax.annotation.Nonnull;

public class BuilderActionTakeItemFromChestTask extends BuilderActionBase {

   public BuilderActionTakeItemFromChestTask() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Take Item From Chest Task.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionTakeItemFromChestTask(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionTakeItemFromChestTask readConfig(@Nonnull JsonElement data) {
      return this;
   }
}
