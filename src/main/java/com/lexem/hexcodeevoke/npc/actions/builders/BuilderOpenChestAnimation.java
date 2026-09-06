package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionOpenChestAnimation;

import javax.annotation.Nonnull;

public class BuilderOpenChestAnimation extends BuilderActionBase {
   protected boolean reverse;

   public BuilderOpenChestAnimation() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Remove minion task.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionOpenChestAnimation(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderOpenChestAnimation readConfig(@Nonnull JsonElement data) {
      this.getBoolean(
              data,
              "Reverse",
              s -> this.reverse = s,
              false,
              BuilderDescriptorState.Stable,
              "If true, triggers the chest-closing animation instead of the opening one.",
              ""
      );
      return this;
   }

   public boolean getReverse() {
      return this.reverse;
   }
}
