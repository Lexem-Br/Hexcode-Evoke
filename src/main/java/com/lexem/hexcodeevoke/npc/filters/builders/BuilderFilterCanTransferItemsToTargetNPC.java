package com.lexem.hexcodeevoke.npc.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.lexem.hexcodeevoke.npc.filters.FilterCanTransferItemsToTargetNPC;

import javax.annotation.Nonnull;

public class BuilderFilterCanTransferItemsToTargetNPC extends BuilderEntityFilterBase {
   protected boolean reverse;

   public BuilderFilterCanTransferItemsToTargetNPC() {}

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Checks if can transfer items between NPCs.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return getShortDescription();
   }

   @Nonnull
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      return new FilterCanTransferItemsToTargetNPC(this);
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
              "If true, checks if cannot transfer items between NPCs",
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
