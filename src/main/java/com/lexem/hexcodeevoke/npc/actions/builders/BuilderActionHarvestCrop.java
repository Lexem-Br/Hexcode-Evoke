package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionHarvestCrop;
import com.lexem.hexcodeevoke.npc.actions.ActionOpenHCProfile;

import javax.annotation.Nonnull;

public class BuilderActionHarvestCrop extends BuilderActionBase {

   public BuilderActionHarvestCrop() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Harvest the crop.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionHarvestCrop(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionHarvestCrop readConfig(@Nonnull JsonElement data) {
      return this;
   }
}
