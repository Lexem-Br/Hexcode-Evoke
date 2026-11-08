package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.ActionSetMotionController;

import javax.annotation.Nonnull;

public class BuilderActionSetMotionController extends BuilderActionBase {
   protected String motionController;

   public BuilderActionSetMotionController() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Switch the NPC to another of its motion controllers.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionSetMotionController(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionSetMotionController readConfig(@Nonnull JsonElement data) {
      this.requireString(data, "MotionController", s -> this.motionController = s, StringNotEmptyValidator.get(), BuilderDescriptorState.Stable, "Name of the motion controller to activate (e.g. Walk, Fly)", null);
      return this;
   }

   public String getMotionController() {
      return this.motionController;
   }
}
