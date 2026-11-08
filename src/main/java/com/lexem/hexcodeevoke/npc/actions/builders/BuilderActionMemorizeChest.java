package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionMemorizeChest;
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorSearchedChestPosition;

import javax.annotation.Nonnull;

public class BuilderActionMemorizeChest extends BuilderActionBase {
   protected BuilderSensorSearchedChestPosition.ChestTarget target;

   public BuilderActionMemorizeChest() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Memorize the items of the chest the Hex Creature is visiting.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Stores the item ids of the searched or explored chest in the chest memory, forgets it if the chest no longer exists and marks it as visited.";
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionMemorizeChest(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionMemorizeChest readConfig(@Nonnull JsonElement data) {
      this.getEnum(data, "Target", v -> this.target = v, BuilderSensorSearchedChestPosition.ChestTarget.class, BuilderSensorSearchedChestPosition.ChestTarget.Search, BuilderDescriptorState.Stable, "Which chest is memorized", null);
      return this;
   }

   public BuilderSensorSearchedChestPosition.ChestTarget getTarget() {
      return this.target;
   }
}
