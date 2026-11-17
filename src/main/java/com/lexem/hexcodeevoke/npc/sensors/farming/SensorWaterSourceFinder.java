package com.lexem.hexcodeevoke.npc.sensors.farming;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.lexem.hexcodeevoke.npc.sensors.BlockFinderSensor;
import com.lexem.hexcodeevoke.npc.sensors.farming.builders.BuilderSensorWaterSourceFinder;
import com.lexem.hexcodeevoke.utils.farming.WateringCanUtils;
import com.lexem.hexcodeevoke.utils.inventory.HeldItemUtils;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.List;

public class SensorWaterSourceFinder extends BlockFinderSensor {

    public SensorWaterSourceFinder(@Nonnull BuilderSensorWaterSourceFinder builder, @Nonnull BuilderSupport support) {
        super(builder, builder.getHorizontalRange(support), builder.getVerticalRange(support));
    }

    @Override
    protected boolean canSearch(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        return HeldItemUtils.findUsableHeldItem(ref, store, List.of(WateringCanUtils.EMPTY_CAN_ID)) != null;
    }

    @Override
    protected boolean isTargetBlock(@Nonnull Vector3i block, @Nonnull World world) {
        return WateringCanUtils.isWater(block, world);
    }
}
