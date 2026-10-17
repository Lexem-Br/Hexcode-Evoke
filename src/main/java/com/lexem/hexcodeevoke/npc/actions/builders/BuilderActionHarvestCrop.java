package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.ItemExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionHarvestCrop;

import javax.annotation.Nonnull;
import java.util.EnumSet;

public class BuilderActionHarvestCrop extends BuilderActionBase {
   protected final AssetArrayHolder requiredItems = new AssetArrayHolder();

   public BuilderActionHarvestCrop() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Harvest the crop.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionHarvestCrop(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionHarvestCrop readConfig(@Nonnull JsonElement data) {
      this.getAssetArray(
         data,
         "RequiredItems",
         this.requiredItems,
         null,
         0,
         Integer.MAX_VALUE,
         ItemExistsValidator.withConfig(EnumSet.of(AssetValidator.Config.LIST_NULLABLE, AssetValidator.Config.LIST_CAN_BE_EMPTY, AssetValidator.Config.MATCHER)),
         BuilderDescriptorState.Stable,
         "Items that the NPC must be holding to harvest",
         "Items that the NPC must be holding in one of their hands to perform the collection. If omitted, no item is required"
      );
      return this;
   }

   public String[] getRequiredItems(@Nonnull BuilderSupport support) {
      return this.requiredItems.get(support.getExecutionContext());
   }
}
