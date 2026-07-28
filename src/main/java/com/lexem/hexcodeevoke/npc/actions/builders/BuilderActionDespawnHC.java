package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.lexem.hexcodeevoke.npc.actions.ActionDespawnHC;

import javax.annotation.Nonnull;

public class BuilderActionDespawnHC extends BuilderActionBase {
   protected boolean force;

   public BuilderActionDespawnHC() {
   }

   @Nonnull
   public ActionDespawnHC build(BuilderSupport builderSupport) {
      return new ActionDespawnHC(this);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Trigger the NPC to despawn";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Trigger the NPC to start the despawning cycle. If the script contains a despawn sensor it will run that action/motion before removing.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionDespawnHC readConfig(@Nonnull JsonElement data) {
      this.getBoolean(data, "Force", b -> this.force = b, false, BuilderDescriptorState.Stable, "Force the NPC to remove automatically", null);
      return this;
   }

   public boolean isForced() {
      return this.force;
   }
}
