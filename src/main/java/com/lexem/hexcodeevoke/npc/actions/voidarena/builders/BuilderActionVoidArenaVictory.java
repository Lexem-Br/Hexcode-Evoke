package com.lexem.hexcodeevoke.npc.actions.voidarena.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.voidarena.ActionVoidArenaVictory;

import javax.annotation.Nonnull;
import java.util.EnumSet;

public class BuilderActionVoidArenaVictory extends BuilderActionBase {

   public BuilderActionVoidArenaVictory() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Finish the Void Arena fight with a victory.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Encounter-only action used by Evoke_Void_Arena_Waves. Spends the altar, places the reward chest next to it and shows the victory title.";
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionVoidArenaVictory(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionVoidArenaVictory readConfig(@Nonnull JsonElement data) {
      this.requireInstructionType(EnumSet.of(InstructionType.Encounter, InstructionType.EncounterStateTransitions));
      return this;
   }
}
