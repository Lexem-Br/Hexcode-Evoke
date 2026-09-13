package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionStoreItems;

import javax.annotation.Nonnull;

public class BuilderActionStoreItems extends BuilderActionBase {
   protected boolean skipHotbarSlotZero = true;

   public BuilderActionStoreItems() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Store items.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionStoreItems(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionStoreItems readConfig(@Nonnull JsonElement data) {
      this.getBoolean(
              data,
              "SkipHotbarSlotZero",
              b -> this.skipHotbarSlotZero = b,
              true,
              BuilderDescriptorState.Stable,
              "Field that determines whether the NPC's hotbar slot 0 should be ignored when storing items.",
              null
      );
      return this;
   }

   public boolean getSkipHotbarSlotZero() {
      return this.skipHotbarSlotZero;
   }
}
