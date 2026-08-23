package com.lexem.hexcodeevoke.npc.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.lexem.hexcodeevoke.npc.filters.FilterEvokeIsInvetoryFull;
import com.lexem.hexcodeevoke.npc.filters.validators.InventoryTypeExistsValidator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

public class BuilderFilterIsInventoryFull extends BuilderEntityFilterBase {
   protected final AssetArrayHolder inventoryTypes = new AssetArrayHolder();

   public BuilderFilterIsInventoryFull() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Checks if a specific type of inventory is full";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return getShortDescription();
   }

   @Nonnull
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      return new FilterEvokeIsInvetoryFull(this, builderSupport);
   }
   @Nonnull
   @Override
   public Builder<IEntityFilter> readConfig(@Nonnull JsonElement data) {
      this.getAssetArray(
              data,
              "Inventories",
              this.inventoryTypes,
              null,
              1,
              Integer.MAX_VALUE,
              InventoryTypeExistsValidator.withConfig(EnumSet.of(AssetValidator.Config.MATCHER)),
              BuilderDescriptorState.Stable,
              "A set of inventory types to include",
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
   public String[] getInventoryTypes(@Nonnull BuilderSupport support) {
      return this.inventoryTypes.get(support.getExecutionContext());
   }
}
