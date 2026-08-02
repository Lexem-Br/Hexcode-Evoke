package com.lexem.hexcodeevoke.npc.filters.validators;

import com.hypixel.hytale.server.core.asset.type.item.config.ItemDropList;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;

import javax.annotation.Nonnull;
import java.util.EnumSet;

public class InventoryTypeExistsValidator extends AssetValidator {
   private static final InventoryTypeExistsValidator DEFAULT_INSTANCE = new InventoryTypeExistsValidator();

   private InventoryTypeExistsValidator() {
   }

   private InventoryTypeExistsValidator(EnumSet<AssetValidator.Config> config) {
      super(config);
   }

   @Nonnull
   @Override
   public String getDomain() {
      return "InventoryTypeList";
   }

   @Override
   public boolean test(String value) {
       return value.equals("Armor") || value.equals("Hotbar") || value.equals("Storage")
               || value.equals("Utility") || value.equals("Tool") || value.equals("Backpack");
   }

   @Nonnull
   @Override
   public String errorMessage(String value, String attribute) {
      return "The inventory type with the name \"" + value + "\" does not exist for attribute \"" + attribute + "\"";
   }

   @Nonnull
   @Override
   public String getAssetName() {
      return ItemDropList.class.getSimpleName();
   }

   public static InventoryTypeExistsValidator required() {
      return DEFAULT_INSTANCE;
   }

   @Nonnull
   public static InventoryTypeExistsValidator withConfig(EnumSet<AssetValidator.Config> config) {
      return new InventoryTypeExistsValidator(config);
   }
}
