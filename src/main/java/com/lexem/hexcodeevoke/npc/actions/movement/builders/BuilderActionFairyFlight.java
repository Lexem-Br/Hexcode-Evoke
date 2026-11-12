package com.lexem.hexcodeevoke.npc.actions.movement.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.ParticleSystemExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.lexem.hexcodeevoke.npc.actions.movement.ActionFairyFlight;
import com.lexem.hexcodeevoke.utils.FairyFlightSettings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderActionFairyFlight extends BuilderActionBase {
   protected final DoubleHolder gain = new DoubleHolder();
   protected final DoubleHolder maxSpeed = new DoubleHolder();
   protected final DoubleHolder acceleration = new DoubleHolder();
   protected final DoubleHolder dashDistance = new DoubleHolder();
   protected final DoubleHolder dashStopDistance = new DoubleHolder();
   protected final DoubleHolder dashGain = new DoubleHolder();
   protected final DoubleHolder dashSpeed = new DoubleHolder();
   protected final DoubleHolder dashAcceleration = new DoubleHolder();
   protected final DoubleHolder wobbleRadius = new DoubleHolder();
   protected final DoubleHolder wobblePeriod = new DoubleHolder();
   protected final DoubleHolder bobHeight = new DoubleHolder();
   protected final DoubleHolder bobPeriod = new DoubleHolder();
   protected final DoubleHolder flitRadius = new DoubleHolder();
   protected final DoubleHolder flitMinInterval = new DoubleHolder();
   protected final DoubleHolder flitMaxInterval = new DoubleHolder();
   protected final DoubleHolder personalRadius = new DoubleHolder();
   protected final DoubleHolder avoidRadius = new DoubleHolder();
   protected final DoubleHolder maxTilt = new DoubleHolder();
   protected final DoubleHolder tiltFullSpeed = new DoubleHolder();
   protected final DoubleHolder trailMinSpeed = new DoubleHolder();
   protected final DoubleHolder trailInterval = new DoubleHolder();
   protected final AssetHolder trailParticle = new AssetHolder();
   protected boolean matchEvokerLook = false;
   protected boolean hover = false;
   protected boolean ignoreBlocks = false;

   public BuilderActionFairyFlight() {
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Fly like a fairy to the sensor position.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Moves the Hex Creature with its own velocity, independent from where the body is facing, so it goes straight to the sensor position instead of turning in wide circles. Adds a wobble, short random darts, a fast dash when far away and tilts the body towards its movement.";
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionFairyFlight(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionFairyFlight readConfig(@Nonnull JsonElement data) {
      this.getDouble(data, "Gain", this.gain, 7.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Speed per block of distance to the target", "Blocks per second for each block away from the target. Higher values arrive faster and trail closer behind a moving target");
      this.getDouble(data, "MaxSpeed", this.maxSpeed, 15.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum speed while close to the target", null);
      this.getDouble(data, "Acceleration", this.acceleration, 60.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "How fast the velocity changes while close to the target", "Blocks per second squared, in any direction. Higher values make sharper changes of direction");
      this.getDouble(data, "DashDistance", this.dashDistance, 3.5, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Distance to the target that starts a dash", null);
      this.getDouble(data, "DashStopDistance", this.dashStopDistance, 1.2, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Distance to the target that ends a dash", null);
      this.getDouble(data, "DashGain", this.dashGain, 9.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Speed per block of distance to the target while dashing", null);
      this.getDouble(data, "DashSpeed", this.dashSpeed, 22.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum speed while dashing", null);
      this.getDouble(data, "DashAcceleration", this.dashAcceleration, 90.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "How fast the velocity changes while dashing", null);
      this.getDouble(data, "WobbleRadius", this.wobbleRadius, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Radius of the slow horizontal drift around the target", null);
      this.getDouble(data, "WobblePeriod", this.wobblePeriod, 3.5, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Seconds of a horizontal drift cycle", null);
      this.getDouble(data, "BobHeight", this.bobHeight, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Height of the up and down motion around the target", null);
      this.getDouble(data, "BobPeriod", this.bobPeriod, 1.8, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Seconds of an up and down cycle", null);
      this.getDouble(data, "FlitRadius", this.flitRadius, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "How far the short random darts reach", "Every FlitMinInterval to FlitMaxInterval seconds a new point around the target is chosen and the creature darts there. 0 disables it");
      this.getDouble(data, "FlitMinInterval", this.flitMinInterval, 1.2, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Minimum seconds between darts", null);
      this.getDouble(data, "FlitMaxInterval", this.flitMaxInterval, 3.2, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum seconds between darts", null);
      this.getDouble(data, "PersonalRadius", this.personalRadius, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Radius of a fixed random offset for each creature", "Keeps creatures that share the same target apart");
      this.getDouble(data, "AvoidRadius", this.avoidRadius, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Distance kept from the evoker's head", "When the straight line to the target passes closer than this to the evoker's head the creature arcs over it. 0 disables it");
      this.getDouble(data, "MaxTilt", this.maxTilt, 35.0, DoubleRangeValidator.between(0.0, 90.0), BuilderDescriptorState.Stable, "Maximum degrees the body leans towards its movement", null);
      this.getDouble(data, "TiltFullSpeed", this.tiltFullSpeed, 9.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Speed at which the body reaches MaxTilt", null);
      this.getAsset(data, "TrailParticle", this.trailParticle, "", ParticleSystemExistsValidator.withConfig(AssetValidator.CanBeEmpty), BuilderDescriptorState.Stable, "Particle system left behind while flying fast", "Tinted with the color of the model's light. Empty disables it");
      this.getDouble(data, "TrailMinSpeed", this.trailMinSpeed, 5.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Minimum speed that leaves a trail", null);
      this.getDouble(data, "TrailInterval", this.trailInterval, 0.07, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Seconds between trail particles", null);
      this.getBoolean(data, "MatchEvokerLook", aBoolean -> this.matchEvokerLook = aBoolean, false, BuilderDescriptorState.Stable, "Looks in the same direction as the evoker while hovering", "The body faces the movement above 5 blocks per second");
      this.getBoolean(data, "Hover", aBoolean -> this.hover = aBoolean, false, BuilderDescriptorState.Stable, "Stays around the position where the action started", "The sensor position is not used");
      this.getBoolean(data, "IgnoreBlocks", aBoolean -> this.ignoreBlocks = aBoolean, false, BuilderDescriptorState.Stable, "Flies through blocks", "Without it the creature slides along blocks, goes up to clear low obstacles and only goes through them when stuck");
      return this;
   }

   @Nonnull
   public FairyFlightSettings getSettings(@Nonnull BuilderSupport support) {
      return new FairyFlightSettings(
              this.gain.get(support.getExecutionContext()),
              this.maxSpeed.get(support.getExecutionContext()),
              this.acceleration.get(support.getExecutionContext()),
              this.dashDistance.get(support.getExecutionContext()),
              this.dashStopDistance.get(support.getExecutionContext()),
              this.dashGain.get(support.getExecutionContext()),
              this.dashSpeed.get(support.getExecutionContext()),
              this.dashAcceleration.get(support.getExecutionContext()),
              this.wobbleRadius.get(support.getExecutionContext()),
              this.wobblePeriod.get(support.getExecutionContext()),
              this.bobHeight.get(support.getExecutionContext()),
              this.bobPeriod.get(support.getExecutionContext()),
              this.flitRadius.get(support.getExecutionContext()),
              this.flitMinInterval.get(support.getExecutionContext()),
              this.flitMaxInterval.get(support.getExecutionContext()),
              this.personalRadius.get(support.getExecutionContext()),
              this.avoidRadius.get(support.getExecutionContext()),
              Math.toRadians(this.maxTilt.get(support.getExecutionContext())),
              this.tiltFullSpeed.get(support.getExecutionContext()),
              this.matchEvokerLook
      );
   }

   @Nullable
   public String getTrailParticle(@Nonnull BuilderSupport support) {
      String particle = this.trailParticle.get(support.getExecutionContext());
      return particle == null || particle.isEmpty() ? null : particle;
   }

   public double getTrailMinSpeed(@Nonnull BuilderSupport support) {
      return this.trailMinSpeed.get(support.getExecutionContext());
   }

   public double getTrailInterval(@Nonnull BuilderSupport support) {
      return this.trailInterval.get(support.getExecutionContext());
   }

   public boolean isHover() {
      return this.hover;
   }

   public boolean isIgnoreBlocks() {
      return this.ignoreBlocks;
   }
}
