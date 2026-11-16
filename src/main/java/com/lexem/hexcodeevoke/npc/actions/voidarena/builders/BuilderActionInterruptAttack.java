package com.lexem.hexcodeevoke.npc.actions.voidarena.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.voidarena.ActionInterruptAttack;

import javax.annotation.Nonnull;

public class BuilderActionInterruptAttack extends BuilderActionBase {

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Cancel the attack the NPC is running.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Cancels the running attack chain and clears the attack pause, so the next Attack action starts on the same tick instead of waiting for the current attack to end.";
   }

   @Nonnull
   @Override
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionInterruptAttack(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public BuilderActionInterruptAttack readConfig(@Nonnull JsonElement data) {
      return this;
   }
}
