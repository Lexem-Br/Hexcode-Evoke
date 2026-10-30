package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionOpenArcaneCodexSearch;

import javax.annotation.Nonnull;

public class BuilderActionOpenArcaneCodexSearch extends BuilderActionBase {

   public BuilderActionOpenArcaneCodexSearch() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Open the Arcane Codex item search page.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Shows every item the Hex Creature remembers from the chests, the selected item is searched in the nearest chest that has it.";
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionOpenArcaneCodexSearch(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionOpenArcaneCodexSearch readConfig(@Nonnull JsonElement data) {
      return this;
   }
}
