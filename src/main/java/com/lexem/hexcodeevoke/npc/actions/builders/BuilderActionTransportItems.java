package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionTransportItems;
import com.lexem.hexcodeevoke.utils.TransportState;

import javax.annotation.Nonnull;

public class BuilderActionTransportItems extends BuilderActionBase {
   protected TransportState.Target target;
   protected final DoubleHolder range = new DoubleHolder();

   public BuilderActionTransportItems() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Move items between the Hex Creature and a container chosen with the wand.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Deliver stores the carried items the destination accepts; Collect takes from the source the items the destination accepts, as many as fit. "
              + "Never touches the hand slot. When nothing is moved, Evoke:TransportTarget with Ready stops matching for a few seconds";
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionTransportItems(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionTransportItems readConfig(@Nonnull JsonElement data) {
      this.getEnum(data, "Target", v -> this.target = v, TransportState.Target.class, TransportState.Target.Deliver, BuilderDescriptorState.Stable, "Deliver or Collect", null);
      this.getDouble(data, "Range", this.range, 3.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum distance to the container", null);
      return this;
   }

   public TransportState.Target getTarget() {
      return this.target;
   }

   public double getRange(@Nonnull BuilderSupport support) {
      return this.range.get(support.getExecutionContext());
   }
}
