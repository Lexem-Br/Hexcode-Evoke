package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionSendOrderToMinion;

import javax.annotation.Nonnull;

public class BuilderActionSendOrderToMinion extends BuilderActionBase {

   public BuilderActionSendOrderToMinion() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Sends an order to a minion to search for or store an item.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionSendOrderToMinion(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionSendOrderToMinion readConfig(@Nonnull JsonElement data) {
      return this;
   }
}
