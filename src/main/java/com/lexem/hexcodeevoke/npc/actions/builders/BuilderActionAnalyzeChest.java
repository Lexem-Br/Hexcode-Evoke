package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionAnalyzeChest;

import javax.annotation.Nonnull;

public class BuilderActionAnalyzeChest extends BuilderActionBase {

   public BuilderActionAnalyzeChest() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Analyze chest.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionAnalyzeChest(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionAnalyzeChest readConfig(@Nonnull JsonElement data) {
      return this;
   }

}
