package com.lexem.hexcodeevoke.npc.actions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import com.lexem.hexcodeevoke.npc.actions.ActionTargetPlayAnimation;
import com.hypixel.hytale.protocol.AnimationSlot;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class BuilderActionTargetPlayAnimation extends BuilderActionBase {
   protected NPCAnimationSlot slot;
   protected final StringHolder animationId = new StringHolder();

   public BuilderActionTargetPlayAnimation() {
   }

   public ActionTargetPlayAnimation build(@Nonnull BuilderSupport builderSupport) {
      return new ActionTargetPlayAnimation(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Make the target play an animation.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return getShortDescription();
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Experimental;
   }

   @Nonnull
   public BuilderActionTargetPlayAnimation readConfig(@Nonnull JsonElement data) {
      this.getEnum(
              data,
              "Slot",
              v -> this.slot = v,
              NPCAnimationSlot.class,
              NPCAnimationSlot.Status,
              BuilderDescriptorState.Stable,
              "The animation slot to play on",
              null
      );
      this.getString(
              data,
              "Animation",
              this.animationId,
              null,
              null,
              BuilderDescriptorState.Stable,
              "The animation ID to play",
              null);
      return this;
   }

   @Override
   protected void runLoadTimeValidationHelper0(
           String configName, @Nonnull NPCLoadTimeValidationHelper loadTimeValidationHelper, ExecutionContext context, List<String> errors
   ) {
      loadTimeValidationHelper.validateAnimation(this.animationId.get(context));
   }

   @Nullable
   public String getAnimationId(@Nonnull BuilderSupport support) {
      String anim = this.animationId.get(support.getExecutionContext());
      return anim != null && !anim.isEmpty() ? anim : null;
   }

   public NPCAnimationSlot getSlot() {
      return this.slot;
   }

   public enum NPCAnimationSlot implements Supplier<String> {
      Status(AnimationSlot.Status),
      Action(AnimationSlot.Action),
      Face(AnimationSlot.Face);

      public static final NPCAnimationSlot[] VALUES = values();
      private final AnimationSlot mappedSlot;

      NPCAnimationSlot(AnimationSlot mappedSlot) {
         this.mappedSlot = mappedSlot;
      }

      @Nonnull
      public String get() {
         return this.name();
      }

      public AnimationSlot getMappedSlot() {
         return this.mappedSlot;
      }
   }

}
