package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionFinishRefuel;

import javax.annotation.Nonnull;

public class BuilderActionFinishRefuel extends BuilderActionBase {
   protected String defaultState = "Following";

   public BuilderActionFinishRefuel() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Leave the Refuel state and go back to the state the Hex Creature was in.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Restores the state and sub state remembered by Evoke:StartRefuel, or DefaultState when there is none. Afterwards Evoke:NeedsEssenceTray waits a while before matching again";
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionFinishRefuel(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionFinishRefuel readConfig(@Nonnull JsonElement data) {
      this.getString(data, "DefaultState", s -> this.defaultState = s, "Following", StringNotEmptyValidator.get(), BuilderDescriptorState.Stable, "State used when there is no state to go back to", null);
      return this;
   }

   public String getDefaultState() {
      return this.defaultState;
   }
}
