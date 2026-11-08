package com.lexem.hexcodeevoke.npc.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.lexem.hexcodeevoke.npc.filters.FilterHasItemsOnInventory;
import com.lexem.hexcodeevoke.npc.filters.validators.InventoryTypeExistsValidator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

public class BuilderFilterHasItemsOnInventory extends BuilderEntityFilterBase {
   private static final String[] DEFAULT_INVENTORIES = new String[]{"Hotbar", "Storage"};

   protected final AssetArrayHolder inventoryTypes = new AssetArrayHolder();
   protected boolean skipHotbarSlotZero;

   public BuilderFilterHasItemsOnInventory() {}

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Checks if there are items on the given inventory types";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return getShortDescription();
   }

   @Nonnull
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      return new FilterHasItemsOnInventory(this, builderSupport);
   }
   @Nonnull
   @Override
   public Builder<IEntityFilter> readConfig(@Nonnull JsonElement data) {
      this.getAssetArray(
              data,
              "Inventories",
              this.inventoryTypes,
              DEFAULT_INVENTORIES,
              1,
              Integer.MAX_VALUE,
              InventoryTypeExistsValidator.withConfig(EnumSet.of(AssetValidator.Config.MATCHER)),
              BuilderDescriptorState.Stable,
              "A set of inventory types to check",
              "A set of inventory types to check (Armor, Hotbar, Storage, Utility, Tool, Backpack). Defaults to Hotbar and Storage"
      );
      this.getBoolean(
              data,
              "SkipHotbarSlotZero",
              b -> this.skipHotbarSlotZero = b,
              false,
              BuilderDescriptorState.Stable,
              "Whether the NPC's hotbar slot 0 should be ignored.",
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

   public boolean getSkipHotbarSlotZero() {
      return this.skipHotbarSlotZero;
   }
}
