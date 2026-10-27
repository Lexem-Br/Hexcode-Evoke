package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionTakeTrayEssence;

import javax.annotation.Nonnull;

public class BuilderActionTakeTrayEssence extends BuilderActionBase {

   public BuilderActionTakeTrayEssence() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Consume as much life essence from the Essence Tray as the Hex Creature can store.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Takes whole units from the tray chosen by Evoke:StartRefuel, up to what is missing from the life essence storage";
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionTakeTrayEssence(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionTakeTrayEssence readConfig(@Nonnull JsonElement data) {
      return this;
   }
}
