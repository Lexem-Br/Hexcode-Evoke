package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionSnapToBlockCenter;

import javax.annotation.Nonnull;

public class BuilderActionSnapToBlockCenter extends BuilderActionBase {
   protected boolean snapRotation = true;

   public BuilderActionSnapToBlockCenter() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Move the NPC to the center of the block it is standing in.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Centers the NPC horizontally on its current block, keeping its height, and optionally turns it to the closest of the four block directions so it looks like a placed block";
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionSnapToBlockCenter(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionSnapToBlockCenter readConfig(@Nonnull JsonElement data) {
      this.getBoolean(data, "SnapRotation", aBoolean -> this.snapRotation = aBoolean, true, BuilderDescriptorState.Stable, "Turns the NPC to the closest of the four block directions", null);
      return this;
   }

   public boolean getSnapRotation() {
      return this.snapRotation;
   }
}
