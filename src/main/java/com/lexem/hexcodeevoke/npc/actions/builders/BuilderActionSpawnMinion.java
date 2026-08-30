package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionSpawnMinion;

import javax.annotation.Nonnull;

public class BuilderActionSpawnMinion extends BuilderActionBase {
   protected final StringHolder entityId = new StringHolder();

   public BuilderActionSpawnMinion() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Spawn a minion for a Hex Creature.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionSpawnMinion(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionSpawnMinion readConfig(@Nonnull JsonElement data) {
      this.requireString(data, "EntityId", this.entityId, null, BuilderDescriptorState.Stable, "Entity id from NPC to spawn", null);
      return this;
   }

   public String getEntityId(@Nonnull BuilderSupport support) {
      return this.entityId.get(support.getExecutionContext());
   }
}
