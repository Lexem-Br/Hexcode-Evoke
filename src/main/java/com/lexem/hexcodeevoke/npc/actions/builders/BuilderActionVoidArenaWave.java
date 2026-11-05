package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import com.hypixel.hytale.server.npc.asset.builder.holder.IntHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.IntSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionVoidArenaWave;

import javax.annotation.Nonnull;
import java.util.EnumSet;

public class BuilderActionVoidArenaWave extends BuilderActionBase {
   protected final IntHolder wave = new IntHolder();
   protected final IntHolder totalWaves = new IntHolder();
   protected final boolean cleared;

   public BuilderActionVoidArenaWave(boolean cleared) {
      this.cleared = cleared;
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return cleared ? "Tell the Void Arena that a wave was cleared." : "Tell the Void Arena that a wave started.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Encounter-only action used by Evoke_Void_Arena_Waves. Shows the wave title and plays the wave effects to every player in the arena instance.";
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionVoidArenaWave(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionVoidArenaWave readConfig(@Nonnull JsonElement data) {
      this.requireInt(data, "Wave", this.wave, IntSingleValidator.greater0(), BuilderDescriptorState.Stable, "Number of the wave, starting at 1", null);
      this.requireInt(data, "TotalWaves", this.totalWaves, IntSingleValidator.greater0(), BuilderDescriptorState.Stable, "Number of waves in the fight", null);
      this.requireInstructionType(EnumSet.of(InstructionType.Encounter, InstructionType.EncounterStateTransitions));
      return this;
   }

   public boolean isCleared() {
      return cleared;
   }

   public int getWave(@Nonnull BuilderSupport support) {
      return this.wave.get(support.getExecutionContext());
   }

   public int getTotalWaves(@Nonnull BuilderSupport support) {
      return this.totalWaves.get(support.getExecutionContext());
   }
}
