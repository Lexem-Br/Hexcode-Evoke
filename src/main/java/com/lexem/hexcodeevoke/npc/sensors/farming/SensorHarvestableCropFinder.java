package com.lexem.hexcodeevoke.npc.sensors.farming;

import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.lexem.hexcodeevoke.npc.sensors.BlockFinderSensor;
import com.lexem.hexcodeevoke.npc.sensors.farming.builders.BuilderSensorHarvestableCropFinder;
import com.lexem.hexcodeevoke.utils.farming.CropUtils;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.List;

public class SensorHarvestableCropFinder extends BlockFinderSensor {
    private final List<String> soilBlocks;

    public SensorHarvestableCropFinder(@Nonnull BuilderSensorHarvestableCropFinder builder, @Nonnull BuilderSupport support) {
        super(builder, builder.getHorizontalRange(support), builder.getVerticalRange(support));
        String[] soils = builder.getSoilBlocks(support);
        this.soilBlocks = soils != null && soils.length > 0 ? List.of(soils) : null;
    }

    @Override
    protected boolean isTargetBlock(@Nonnull Vector3i block, @Nonnull World world) {
        return CropUtils.isHarvestableCrop(block, world, soilBlocks);
    }
}
