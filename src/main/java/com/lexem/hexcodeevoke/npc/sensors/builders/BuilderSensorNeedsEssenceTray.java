package com.lexem.hexcodeevoke.npc.sensors.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.lexem.hexcodeevoke.npc.sensors.SensorNeedsEssenceTray;

import javax.annotation.Nonnull;

public class BuilderSensorNeedsEssenceTray extends BuilderSensorBase {
    protected final DoubleHolder range = new DoubleHolder();
    protected final DoubleHolder threshold = new DoubleHolder();

    public BuilderSensorNeedsEssenceTray() {
    }

    @Nonnull
    @Override
    public String getShortDescription() {
        return "Matches when the Hex Creature is low on life essence and an Essence Tray with life essence is nearby.";
    }

    @Nonnull
    @Override
    public String getLongDescription() {
        return "Checked once per second. Never matches while the Hex Creature is already in the Refuel state, shortly after a refuel, or for creatures that do not consume life essence. The position is the nearest tray";
    }

    @Nonnull
    public Sensor build(@Nonnull BuilderSupport builderSupport) {
        return new SensorNeedsEssenceTray(this, builderSupport);
    }

    @Nonnull
    @Override
    public BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Stable;
    }

    @Nonnull
    @Override
    public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
        this.getDouble(data, "Range", this.range, 15.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum distance to the tray", null);
        this.getDouble(data, "Threshold", this.threshold, 0.5, DoubleRangeValidator.between(0.0, 1.0), BuilderDescriptorState.Stable, "Fraction of the life essence capacity at or below which the Hex Creature looks for a tray", null);
        this.provideFeature(Feature.Position);
        return this;
    }

    public double getRange(@Nonnull BuilderSupport support) {
        return this.range.get(support.getExecutionContext());
    }

    public double getThreshold(@Nonnull BuilderSupport support) {
        return this.threshold.get(support.getExecutionContext());
    }
}
