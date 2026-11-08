package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.DespawnHC;

import javax.annotation.Nonnull;

public class BuilderDespawnHC extends BuilderActionBase {
   protected boolean dropHexItem = false;

   public BuilderDespawnHC() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Despawn a Hex Creature.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new DespawnHC(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderDespawnHC readConfig(@Nonnull JsonElement data) {
      this.getBoolean(
              data,
              "DropHexItem",
              b -> this.dropHexItem = b,
              false,
              BuilderDescriptorState.Stable,
              "Determines whether the Hex Creature item should be dropped.",
              null
      );
      return this;
   }

   public boolean getDropHexItem() {
      return this.dropHexItem;
   }

}
