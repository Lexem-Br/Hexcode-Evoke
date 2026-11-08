package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionOpenChestAnimation;
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorSearchedChestPosition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderActionOpenChestAnimation extends BuilderActionBase {
   protected boolean reverse;
   @Nullable
   protected BuilderSensorSearchedChestPosition.ChestTarget target;

   public BuilderActionOpenChestAnimation() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Remove minion task.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionOpenChestAnimation(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionOpenChestAnimation readConfig(@Nonnull JsonElement data) {
      this.getBoolean(
              data,
              "Reverse",
              s -> this.reverse = s,
              false,
              BuilderDescriptorState.Stable,
              "If true, triggers the chest-closing animation instead of the opening one.",
              ""
      );
      this.getEnum(data, "Target", v -> this.target = v, BuilderSensorSearchedChestPosition.ChestTarget.class, null, BuilderDescriptorState.Stable, "Chest stored in the Hex Creature to animate", "When omitted, the chest at the sensor position is animated");
      return this;
   }

   public boolean getReverse() {
      return this.reverse;
   }

   @Nullable
   public BuilderSensorSearchedChestPosition.ChestTarget getTarget() {
      return this.target;
   }
}
