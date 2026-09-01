package com.lexem.hexcodeevoke.npc.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.lexem.hexcodeevoke.components.HexCreatureMinionComponent;
import com.lexem.hexcodeevoke.npc.filters.FilterHasMinionOnStatus;

import javax.annotation.Nonnull;

public class BuilderFilterHasMinionOnStatus extends BuilderEntityFilterBase {
   protected HexCreatureMinionComponent.Status status;

   public BuilderFilterHasMinionOnStatus() {}

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Checks if there is any minion with this status";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return getShortDescription();
   }

   @Nonnull
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      return new FilterHasMinionOnStatus(this);
   }

   @Nonnull
   @Override
   public Builder<IEntityFilter> readConfig(@Nonnull JsonElement data) {
      this.getEnum(
              data,
              "Status",
              v -> this.status = v,
              HexCreatureMinionComponent.Status.class,
              HexCreatureMinionComponent.Status.Standby,
              BuilderDescriptorState.Stable,
              "Checks if there is any minion with this status",
              null
      );
      return this;
   }

   public HexCreatureMinionComponent.Status getStatus() {
      return this.status;
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }
}
