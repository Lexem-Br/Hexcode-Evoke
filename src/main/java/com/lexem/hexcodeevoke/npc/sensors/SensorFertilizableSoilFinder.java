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
import com.lexem.hexcodeevoke.npc.sensors.builders.BuilderSensorFertilizableSoilFinder;
import com.lexem.hexcodeevoke.utils.FertilizerUtils;
import com.lexem.hexcodeevoke.utils.FinderUtils;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;

public class SensorFertilizableSoilFinder extends SensorBase {
    private final double horizontalRange;
    private final double verticalRange;
    protected boolean wasSteering = false;
    private final PositionProvider positionProvider = new PositionProvider();

    public SensorFertilizableSoilFinder(@Nonnull BuilderSensorFertilizableSoilFinder builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.horizontalRange = builder.getHorizontalRange(support);
        this.verticalRange = builder.getVerticalRange(support);
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull ExecutionSupport executionSupport, double dt, @Nonnull Store<EntityStore> store) {
        if (!super.matches(ref, executionSupport, dt, store) || wasSteering) {
            this.positionProvider.clear();
            return false;
        }

        World world = store.getExternalData().getWorld();
        TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
        if (transformComponent == null) {
            this.positionProvider.clear();
            return false;
        }

        Vector3i soil = FinderUtils.findNearestBlockBFS(
                transformComponent.getPosition(),
                (int) Math.ceil(horizontalRange),
                (int) Math.ceil(verticalRange),
                soilValidator,
                world
        );

        if (soil != null) {
            this.positionProvider.setTarget(new Vector3d(soil.x + 0.5, soil.y, soil.z + 0.5));
        } else {
            this.positionProvider.clear();
        }

        return soil != null;
    }

    private final FinderUtils.BlockValidator<World> soilValidator = FertilizerUtils::isFertilizableSoil;

    public InfoProvider getSensorInfo() { return this.positionProvider; }
}
