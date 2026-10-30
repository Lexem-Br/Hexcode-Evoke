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
import com.lexem.hexcodeevoke.npc.sensors.SensorChestTaskStuck;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class BuilderSensorChestTaskStuck extends BuilderSensorBase {
    protected final DoubleHolder stuckTime = new DoubleHolder();
    protected final DoubleHolder maxTime = new DoubleHolder();
    protected final DoubleHolder minProgress = new DoubleHolder();
    protected Target target;

    public BuilderSensorChestTaskStuck() {
    }

    @Nonnull
    @Override
    public String getShortDescription() {
        return "Test if the minion can't reach the chest of its task or the mother.";
    }

    @Nonnull
    @Override
    public String getLongDescription() {
        return "Matches when the minion hasn't got closer to its target (the chest of its current task, or the mother) for StuckTime seconds, or has been going there for MaxTime seconds. No target is returned.";
    }

    @Nonnull
    public Sensor build(@Nonnull BuilderSupport builderSupport) {
        return new SensorChestTaskStuck(this, builderSupport);
    }

    @Nonnull
    @Override
    public BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Stable;
    }

    @Nonnull
    @Override
    public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
        this.getDouble(data, "StuckTime", this.stuckTime, 8.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Seconds without getting closer to the target before giving up", null);
        this.getDouble(data, "MaxTime", this.maxTime, 60.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum seconds to reach the target before giving up", null);
        this.getDouble(data, "MinProgress", this.minProgress, 0.5, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Distance the minion must get closer to the target to count as progress", null);
        this.getEnum(data, "Target", v -> this.target = v, Target.class, Target.Chest, BuilderDescriptorState.Stable, "What the minion is trying to reach", null);
        this.requireInstructionType(InstructionType.NPCOnlyInstructions);
        return this;
    }

    public double getStuckTime(@Nonnull BuilderSupport support) {
        return this.stuckTime.get(support.getExecutionContext());
    }

    public double getMaxTime(@Nonnull BuilderSupport support) {
        return this.maxTime.get(support.getExecutionContext());
    }

    public double getMinProgress(@Nonnull BuilderSupport support) {
        return this.minProgress.get(support.getExecutionContext());
    }

    public Target getTarget() {
        return this.target;
    }

    public enum Target implements Supplier<String> {
        Chest("The chest of the current task"),
        Owner("The mother");

        private final String description;

        Target(String description) {
            this.description = description;
        }

        @Override
        public String get() {
            return this.description;
        }
    }
}
