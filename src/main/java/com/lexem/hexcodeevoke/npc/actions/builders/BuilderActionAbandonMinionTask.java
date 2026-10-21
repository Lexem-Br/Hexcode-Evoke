package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionAbandonMinionTask;

import javax.annotation.Nonnull;

public class BuilderActionAbandonMinionTask extends BuilderActionBase {
   protected final DoubleHolder unreachableSeconds = new DoubleHolder();
   protected final DoubleHolder teleportDistance = new DoubleHolder();
   protected final DoubleHolder teleportRadius = new DoubleHolder();
   protected boolean forgetChest;

   public BuilderActionAbandonMinionTask() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Give up the minion's chest task and go back to the mother.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Finishes the current chest task without doing it, makes the mother forget the chest and skip it for new tasks for a while, and teleports the minion next to the mother when it is far from her.";
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionAbandonMinionTask(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionAbandonMinionTask readConfig(@Nonnull JsonElement data) {
      this.getDouble(data, "UnreachableSeconds", this.unreachableSeconds, 120.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Seconds new tasks skip the chest that couldn't be reached", null);
      this.getDouble(data, "TeleportDistance", this.teleportDistance, 4.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Teleports only when farther than this from the mother", null);
      this.getDouble(data, "TeleportRadius", this.teleportRadius, 1.5, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Distance from the mother where the minion appears", null);
      this.getBoolean(data, "ForgetChest", v -> this.forgetChest = v, true, BuilderDescriptorState.Stable, "Make the mother forget the task chest and skip it for a while, for when the chest couldn't be reached", null);
      return this;
   }

   public double getUnreachableSeconds(@Nonnull BuilderSupport support) {
      return this.unreachableSeconds.get(support.getExecutionContext());
   }

   public double getTeleportDistance(@Nonnull BuilderSupport support) {
      return this.teleportDistance.get(support.getExecutionContext());
   }

   public double getTeleportRadius(@Nonnull BuilderSupport support) {
      return this.teleportRadius.get(support.getExecutionContext());
   }

   public boolean getForgetChest() {
      return this.forgetChest;
   }
}
