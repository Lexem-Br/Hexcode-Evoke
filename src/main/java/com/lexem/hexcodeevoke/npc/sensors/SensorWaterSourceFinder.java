package com.lexem.hexcodeevoke.npc.sensors;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.PositionProvider;
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorWaterSourceFinder;
import com.lexem.hexcodeevoke.utils.HeldItemUtils;
import com.lexem.hexcodeevoke.utils.WateringCanUtils;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Finds the nearest water to fill the watering can. Only matches while the NPC is holding an empty watering can.
 */
public class SensorWaterSourceFinder extends SensorBase {
    private final double horizontalRange;
    private final double verticalRange;
    private final PositionProvider positionProvider = new PositionProvider();

    public SensorWaterSourceFinder(@Nonnull BuilderSensorWaterSourceFinder builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.horizontalRange = builder.getHorizontalRange(support);
        this.verticalRange = builder.getVerticalRange(support);
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull ExecutionSupport executionSupport, double dt, @Nonnull Store<EntityStore> store) {
        if (!super.matches(ref, executionSupport, dt, store)
                || HeldItemUtils.findUsableHeldItem(ref, store, List.of(WateringCanUtils.EMPTY_CAN_ID)) == null) {
            this.positionProvider.clear();
            return false;
        }

        TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
        if (transformComponent == null) {
            this.positionProvider.clear();
            return false;
        }

        World world = store.getExternalData().getWorld();
        Vector3i water = WateringCanUtils.findNearestWater(transformComponent.getPosition(), horizontalRange, verticalRange, world);

        if (water != null) {
            this.positionProvider.setTarget(new Vector3d(water.x + 0.5, water.y, water.z + 0.5));
        } else {
            this.positionProvider.clear();
        }

        return water != null;
    }

    public InfoProvider getSensorInfo() { return this.positionProvider; }
}
