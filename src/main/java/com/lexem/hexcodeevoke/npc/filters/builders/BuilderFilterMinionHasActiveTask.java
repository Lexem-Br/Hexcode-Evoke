package com.lexem.hexcodeevoke.npc.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.lexem.hexcodeevoke.npc.filters.FilterMinionHasActiveTask;

import javax.annotation.Nonnull;

public class BuilderFilterMinionHasActiveTask extends BuilderEntityFilterBase {
   protected boolean reverse;

   public BuilderFilterMinionHasActiveTask() {}

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Checks if minion has active task";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return getShortDescription();
   }

   @Nonnull
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      return new FilterMinionHasActiveTask(this);
   }

   @Nonnull
   @Override
   public Builder<IEntityFilter> readConfig(@Nonnull JsonElement data) {
      this.getBoolean(
              data,
              "Reverse",
              s -> this.reverse = s,
              false,
              BuilderDescriptorState.Stable,
              "If true, checks if the minion has an active task",
              ""
      );
      return this;
   }

   public boolean getReverse() {
      return this.reverse;
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }
}
