package com.lexem.hexcodeevoke.npc.sensors.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.lexem.hexcodeevoke.npc.sensors.SensorWettableSoilFinder;

import javax.annotation.Nonnull;

public class BuilderSensorWettableSoilFinder extends BuilderSensorBase {
    protected final DoubleHolder horizontalRange = new DoubleHolder();
    protected final DoubleHolder verticalRange = new DoubleHolder();

    public BuilderSensorWettableSoilFinder() {
    }

    @Nonnull
    @Override
    public String getShortDescription() {
        return "Search for dry soil to water.";
    }

    @Nonnull
    @Override
    public String getLongDescription() {
        return this.getShortDescription();
    }

    @Nonnull
    public Sensor build(@Nonnull BuilderSupport builderSupport) {
        return new SensorWettableSoilFinder(this, builderSupport);
    }

    @Nonnull
    @Override
    public BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Stable;
    }

    @Nonnull
    @Override
    public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
        this.requireDouble(data, "HorizontalRange", this.horizontalRange, DoubleRangeValidator.fromExclToIncl(0.0F, Double.MAX_VALUE), BuilderDescriptorState.Stable, "The horizontal range to search", null);
        this.getDouble(data, "VerticalRange", this.verticalRange, 3.0F, DoubleRangeValidator.fromExclToIncl(0.0F, Double.MAX_VALUE), BuilderDescriptorState.Stable, "The vertical range to search", null);
        this.provideFeature(Feature.Position);
        return this;
    }

    public double getHorizontalRange(@Nonnull BuilderSupport support) {
        return this.horizontalRange.get(support.getExecutionContext());
    }

    public double getVerticalRange(@Nonnull BuilderSupport support) {
        return this.verticalRange.get(support.getExecutionContext());
    }
}
