package com.lexem.hexcodeevoke.npc.sensors.farming;

import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.lexem.hexcodeevoke.npc.sensors.BlockFinderSensor;
import com.lexem.hexcodeevoke.npc.sensors.farming.builders.BuilderSensorWettableSoilFinder;
import com.lexem.hexcodeevoke.utils.farming.WateringCanUtils;
import org.joml.Vector3i;

import javax.annotation.Nonnull;

public class SensorWettableSoilFinder extends BlockFinderSensor {
    public SensorWettableSoilFinder(@Nonnull BuilderSensorWettableSoilFinder builder, @Nonnull BuilderSupport support) {
        super(builder, builder.getHorizontalRange(support), builder.getVerticalRange(support));
    }

    @Override
    protected boolean isTargetBlock(@Nonnull Vector3i block, @Nonnull World world) {
        return WateringCanUtils.isWettableSoil(block, world);
    }
}
