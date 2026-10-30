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
import com.lexem.hexcodeevoke.npc.sensors.SensorRefuelPosition;
import com.lexem.hexcodeevoke.utils.RefuelState;

import javax.annotation.Nonnull;

public class BuilderSensorRefuelPosition extends BuilderSensorBase {
    protected RefuelState.Phase target;
    protected final DoubleHolder range = new DoubleHolder();
    protected final DoubleHolder minRange = new DoubleHolder();
    protected final DoubleHolder heightOffset = new DoubleHolder();
    protected final DoubleHolder timeout = new DoubleHolder();

    public BuilderSensorRefuelPosition() {
    }

    @Nonnull
    @Override
    public String getShortDescription() {
        return "Position of the Essence Tray, or of the place to return to, during the Refuel state.";
    }

    @Nonnull
    @Override
    public String getLongDescription() {
        return "Tray matches while the tray chosen by Evoke:StartRefuel still has life essence. Return matches while there is a place to return to, which is never the case when the Hex Creature was following the evoker. Neither matches after Timeout seconds in that phase";
    }

    @Nonnull
    public Sensor build(@Nonnull BuilderSupport builderSupport) {
        return new SensorRefuelPosition(this, builderSupport);
    }

    @Nonnull
    @Override
    public BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Stable;
    }

    @Nonnull
    @Override
    public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
        this.getEnum(data, "Target", v -> this.target = v, RefuelState.Phase.class, RefuelState.Phase.Tray, BuilderDescriptorState.Stable, "Tray or Return", null);
        this.requireDouble(data, "Range", this.range, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum range from the position", null);
        this.getDouble(data, "MinRange", this.minRange, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Minimum range from the position", null);
        this.getDouble(data, "HeightOffset", this.heightOffset, 0.0, DoubleRangeValidator.between(-10.0, 10.0), BuilderDescriptorState.Stable, "Height added to the position", null);
        this.getDouble(data, "Timeout", this.timeout, 30.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Seconds in this phase after which the sensor stops matching, 0 for no limit", null);
        this.provideFeature(Feature.Position);
        return this;
    }

    public RefuelState.Phase getTarget() {
        return this.target;
    }

    public double getRange(@Nonnull BuilderSupport support) {
        return this.range.get(support.getExecutionContext());
    }

    public double getMinRange(@Nonnull BuilderSupport support) {
        return this.minRange.get(support.getExecutionContext());
    }

    public double getHeightOffset(@Nonnull BuilderSupport support) {
        return this.heightOffset.get(support.getExecutionContext());
    }

    public double getTimeout(@Nonnull BuilderSupport support) {
        return this.timeout.get(support.getExecutionContext());
    }
}
