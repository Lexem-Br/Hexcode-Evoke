package com.lexem.hexcodeevoke.npc.sensors.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.lexem.hexcodeevoke.npc.sensors.SensorIsStuck;

import javax.annotation.Nonnull;

public class BuilderSensorIsStuck extends BuilderSensorBase {
    protected final DoubleHolder delay = new DoubleHolder();
    protected final DoubleHolder minProgress = new DoubleHolder();
    protected final DoubleHolder maxStuckTime = new DoubleHolder();

    public BuilderSensorIsStuck() {
    }

    @Nonnull
    @Override
    public String getShortDescription() {
        return "Test if the NPC is stuck against a block.";
    }

    @Nonnull
    @Override
    public String getLongDescription() {
        return "Matches when the active motion controller has been obstructed by blocks for a while without moving horizontally, then keeps matching while the NPC is inside or against blocks. No target is returned.";
    }

    @Nonnull
    public Sensor build(@Nonnull BuilderSupport builderSupport) {
        return new SensorIsStuck(this, builderSupport);
    }

    @Nonnull
    @Override
    public BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Stable;
    }

    @Nonnull
    @Override
    public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
        this.getDouble(data, "Delay", this.delay, 0.3, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Seconds the NPC must be obstructed before it counts as stuck", null);
        this.getDouble(data, "MinProgress", this.minProgress, 0.2, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Horizontal distance the NPC must move to stop counting as stuck", null);
        this.getDouble(data, "MaxStuckTime", this.maxStuckTime, 3.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Seconds the sensor keeps matching after the NPC got stuck, while it is still inside or against blocks", null);
        this.requireInstructionType(InstructionType.NPCOnlyInstructions);
        return this;
    }

    public double getDelay(@Nonnull BuilderSupport support) {
        return this.delay.get(support.getExecutionContext());
    }

    public double getMinProgress(@Nonnull BuilderSupport support) {
        return this.minProgress.get(support.getExecutionContext());
    }

    public double getMaxStuckTime(@Nonnull BuilderSupport support) {
        return this.maxStuckTime.get(support.getExecutionContext());
    }
}
