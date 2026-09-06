package com.lexem.hexcodeevoke.npc.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.lexem.hexcodeevoke.npc.filters.FilterOwnerHasItem;

import javax.annotation.Nonnull;

public class BuilderOwnerHasItem extends BuilderEntityFilterBase {
   protected boolean reverse;

   public BuilderOwnerHasItem() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Checks if the owner has the item.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return getShortDescription();
   }

   @Nonnull
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      return new FilterOwnerHasItem(this);
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
              "If true, it checks whether the owner does not have the item.",
              ""
      );
      return this;
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   public boolean getReverse() {
      return this.reverse;
   }
}
