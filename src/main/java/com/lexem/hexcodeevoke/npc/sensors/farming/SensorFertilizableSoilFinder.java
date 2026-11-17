package com.lexem.hexcodeevoke.npc.sensors.farming;

import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.lexem.hexcodeevoke.npc.sensors.BlockFinderSensor;
import com.lexem.hexcodeevoke.npc.sensors.farming.builders.BuilderSensorFertilizableSoilFinder;
import com.lexem.hexcodeevoke.utils.farming.FertilizerUtils;
import org.joml.Vector3i;

import javax.annotation.Nonnull;

public class SensorFertilizableSoilFinder extends BlockFinderSensor {
    public SensorFertilizableSoilFinder(@Nonnull BuilderSensorFertilizableSoilFinder builder, @Nonnull BuilderSupport support) {
        super(builder, builder.getHorizontalRange(support), builder.getVerticalRange(support));
    }

    @Override
    protected boolean isTargetBlock(@Nonnull Vector3i block, @Nonnull World world) {
        return FertilizerUtils.isFertilizableSoil(block, world);
    }
}
