package com.lexem.hexcodeevoke.npc.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.lexem.hexcodeevoke.components.ChestTaskComponent;
import com.lexem.hexcodeevoke.npc.filters.FilterHasChestTask;

import javax.annotation.Nonnull;

public class BuilderFilterHasChestTask extends BuilderEntityFilterBase {
   protected ChestTaskComponent.TaskType taskType;

   public BuilderFilterHasChestTask() {}

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Checks if has a chest task";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return getShortDescription();
   }

   @Nonnull
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      return new FilterHasChestTask(this);
   }

   @Nonnull
   @Override
   public Builder<IEntityFilter> readConfig(@Nonnull JsonElement data) {
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

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }
}
