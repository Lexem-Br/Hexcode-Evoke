package com.lexem.hexcodeevoke.npc.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.ItemExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.lexem.hexcodeevoke.npc.filters.FilterHasBrokenItemInHand;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

public class BuilderFilterHasBrokenItemInHand extends BuilderEntityFilterBase {
   protected final AssetArrayHolder items = new AssetArrayHolder();

   public BuilderFilterHasBrokenItemInHand() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Checks if the entity is holding one of the items and it is broken";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Checks if the entity is holding one of the items in the main hand or off hand and the item is broken";
   }

   @Nonnull
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      return new FilterHasBrokenItemInHand(this, builderSupport);
   }

   @Nonnull
   @Override
   public Builder<IEntityFilter> readConfig(@Nonnull JsonElement data) {
      this.getAssetArray(
              data,
              "Items",
              this.items,
              null,
              1,
              Integer.MAX_VALUE,
              ItemExistsValidator.withConfig(EnumSet.of(AssetValidator.Config.MATCHER)),
              BuilderDescriptorState.Stable,
              "A set of items the entity must be holding",
              null
      );
      return this;
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nullable
   public String[] getItems(@Nonnull BuilderSupport support) {
      return this.items.get(support.getExecutionContext());
   }
}
