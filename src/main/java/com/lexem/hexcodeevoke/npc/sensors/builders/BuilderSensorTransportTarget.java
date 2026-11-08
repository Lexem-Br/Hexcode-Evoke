package com.lexem.hexcodeevoke.npc.sensors.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.lexem.hexcodeevoke.npc.sensors.SensorTransportTarget;
import com.lexem.hexcodeevoke.utils.TransportState;

import javax.annotation.Nonnull;

public class BuilderSensorTransportTarget extends BuilderSensorBase {
    protected TransportState.Target target;
    protected TransportState.ContainerType containerType;
    protected final DoubleHolder range = new DoubleHolder();
    protected final DoubleHolder minRange = new DoubleHolder();
    protected final DoubleHolder viewRange = new DoubleHolder();
    protected boolean ready;

    public BuilderSensorTransportTarget() {
    }

    @Nonnull
    @Override
    public String getShortDescription() {
        return "Position the Hex Creature goes to while carrying items between the containers chosen with the wand.";
    }

    @Nonnull
    @Override
    public String getLongDescription() {
        return "Matches only while Target is what the Hex Creature should do now: Deliver while it carries items the destination accepts, otherwise Collect (two containers) or Wait (one container). "
                + "The position is the container, or the place it was when the command was given for Wait. A Hex Creature used as a container is only found within ViewRange; "
                + "otherwise the position is where it was last seen. With Ready it also requires the container to be found and the transfer to be possible now";
    }

    @Nonnull
    public Sensor build(@Nonnull BuilderSupport builderSupport) {
        return new SensorTransportTarget(this, builderSupport);
    }

    @Nonnull
    @Override
    public BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Stable;
    }

    @Nonnull
    @Override
    public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
        this.getEnum(data, "Target", v -> this.target = v, TransportState.Target.class, TransportState.Target.Deliver, BuilderDescriptorState.Stable, "Deliver, Collect or Wait", null);
        this.getEnum(data, "Container", v -> this.containerType = v, TransportState.ContainerType.class, TransportState.ContainerType.Any, BuilderDescriptorState.Stable, "Only match when the container is of this type (ignored by Wait)", null);
        this.requireDouble(data, "Range", this.range, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum range from the position", null);
        this.getDouble(data, "MinRange", this.minRange, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Minimum range from the position", null);
        this.getDouble(data, "ViewRange", this.viewRange, 15.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Distance at which a Hex Creature used as a container is noticed", null);
        this.getBoolean(data, "Ready", v -> this.ready = v, false, BuilderDescriptorState.Stable, "Only match when the container is there and items can be moved now", null);
        this.provideFeature(Feature.Position);
        return this;
    }

    public TransportState.Target getTarget() {
        return this.target;
    }

    public TransportState.ContainerType getContainerType() {
        return this.containerType;
    }

    public double getRange(@Nonnull BuilderSupport support) {
        return this.range.get(support.getExecutionContext());
    }

    public double getMinRange(@Nonnull BuilderSupport support) {
        return this.minRange.get(support.getExecutionContext());
    }

    public double getViewRange(@Nonnull BuilderSupport support) {
        return this.viewRange.get(support.getExecutionContext());
    }

    public boolean getReady() {
        return this.ready;
    }
}
