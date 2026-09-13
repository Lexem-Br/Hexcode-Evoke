package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionTransferItemsToTargetNPC;

import javax.annotation.Nonnull;

public class BuilderActionTransferItemsToTargetNPC extends BuilderActionBase {

   public BuilderActionTransferItemsToTargetNPC() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Transfer items to target NPC.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionTransferItemsToTargetNPC(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionTransferItemsToTargetNPC readConfig(@Nonnull JsonElement data) {
      return this;
   }
}
