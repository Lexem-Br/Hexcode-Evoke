package com.lexem.hexcodeevoke.npc.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.ItemExistsValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.lexem.hexcodeevoke.npc.filters.FilterHasHarvestableCrop;

import javax.annotation.Nonnull;
import java.util.EnumSet;

public class BuilderFilterHasHarvestableCrop extends BuilderEntityFilterBase {
   protected final DoubleHolder horizontalRange = new DoubleHolder();
   protected final DoubleHolder verticalRange = new DoubleHolder();
   protected final AssetArrayHolder soilBlocks = new AssetArrayHolder();

   public BuilderFilterHasHarvestableCrop() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Checks if has harvestable crop.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return getShortDescription();
   }

   @Nonnull
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      return new FilterHasHarvestableCrop(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<IEntityFilter> readConfig(@Nonnull JsonElement data) {
      this.requireDouble(data, "HorizontalRange", this.horizontalRange, DoubleRangeValidator.fromExclToIncl(0.0F, Double.MAX_VALUE), BuilderDescriptorState.Stable, "The horizontal range to search", null);
      this.getDouble(data, "VerticalRange", this.verticalRange, 3.0F, DoubleRangeValidator.fromExclToIncl(0.0F, Double.MAX_VALUE), BuilderDescriptorState.Stable, "The vertical range to search", null);
      this.getAssetArray(
         data,
         "SoilBlocks",
         this.soilBlocks,
         null,
         0,
         Integer.MAX_VALUE,
         ItemExistsValidator.withConfig(EnumSet.of(AssetValidator.Config.LIST_NULLABLE, AssetValidator.Config.LIST_CAN_BE_EMPTY)),
         BuilderDescriptorState.Stable,
         "Blocks the crop must be planted on",
         "Only crops planted in one of these blocks are harvested. If this information is omitted, any soil type is accepted"
      );
      return this;
   }

   public double getHorizontalRange(@Nonnull BuilderSupport support) {
      return this.horizontalRange.get(support.getExecutionContext());
   }

   public double getVerticalRange(@Nonnull BuilderSupport support) {
      return this.verticalRange.get(support.getExecutionContext());
   }

   public String[] getSoilBlocks(@Nonnull BuilderSupport support) {
      return this.soilBlocks.get(support.getExecutionContext());
   }
}
