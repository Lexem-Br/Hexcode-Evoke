package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.components.ChestTaskComponent;
import com.lexem.hexcodeevoke.npc.actions.ActionTakeChestTask;

import javax.annotation.Nonnull;

public class BuilderActionTakeChestTask extends BuilderActionBase {
   protected ChestTaskComponent.TaskType taskType;

   public BuilderActionTakeChestTask() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Take a task to complete.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionTakeChestTask(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionTakeChestTask readConfig(@Nonnull JsonElement data) {
      this.getEnum(
              data,
              "TaskType",
              v -> this.taskType = v,
              ChestTaskComponent.TaskType.class,
              ChestTaskComponent.TaskType.Search,
              BuilderDescriptorState.Stable,
              "Specifies the task type",
              null
      );
      return this;
   }

   public ChestTaskComponent.TaskType getTaskType() {
      return this.taskType;
   }


}
